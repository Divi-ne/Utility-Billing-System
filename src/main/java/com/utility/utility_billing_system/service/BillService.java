package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.dto.bill.BillGenerateRequest;
import com.utility.utility_billing_system.dto.bill.BillResponse;
import com.utility.utility_billing_system.entity.Bill;
import com.utility.utility_billing_system.entity.Meter;
import com.utility.utility_billing_system.entity.MeterReading;
import com.utility.utility_billing_system.entity.Tariff;
import com.utility.utility_billing_system.enums.BillStatus;
import com.utility.utility_billing_system.enums.NotificationType;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import com.utility.utility_billing_system.exception.DuplicateResourceException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.mapper.EntityMapper;
import com.utility.utility_billing_system.repository.BillRepository;
import com.utility.utility_billing_system.security.CustomerAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for utility bill generation and lifecycle management.
 * <p>
 * Core billing workflow: reads meter consumption, applies tariff rules (flat or tiered),
 * calculates VAT and service charges, and tracks payment status and penalties.
 */
@Slf4j
@Service
public class BillService {

    private final BillRepository billRepository;
    private final MeterReadingService meterReadingService;
    private final TariffService tariffService;
    private final TariffCalculationService tariffCalculationService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final CustomerAccessService customerAccessService;

    public BillService(
            BillRepository billRepository,
            MeterReadingService meterReadingService,
            TariffService tariffService,
            TariffCalculationService tariffCalculationService,
            NotificationService notificationService,
            EmailService emailService,
            CustomerAccessService customerAccessService) {
        this.billRepository = billRepository;
        this.meterReadingService = meterReadingService;
        this.tariffService = tariffService;
        this.tariffCalculationService = tariffCalculationService;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.customerAccessService = customerAccessService;
    }

    @Transactional(readOnly = true)
    /** Returns all bills for staff reporting. */
    public List<BillResponse> getAllBills() {
        return billRepository.findAll().stream()
                .map(EntityMapper::toBillResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Retrieves a single bill with full charge breakdown. */
    public BillResponse getBillById(Long id) {
        Bill bill = findBill(id);
        customerAccessService.assertOwnBill(bill);
        return EntityMapper.toBillResponse(bill);
    }

    @Transactional(readOnly = true)
    /** Lists all bills for a given customer. */
    public List<BillResponse> getBillsByCustomer(Long customerId) {
        customerAccessService.assertOwnCustomer(customerId);
        return billRepository.findByCustomerId(customerId).stream()
                .map(EntityMapper::toBillResponse)
                .toList();
    }

    @Transactional
    /** Generates a bill from a meter reading using the effective tariff for that period. */
    public BillResponse generateBill(BillGenerateRequest request) {
        MeterReading reading = meterReadingService.findReading(request.getMeterReadingId());
        Meter meter = reading.getMeter();

        if (meter.getCustomer().getStatus() != StatusType.ACTIVE) {
            throw new BusinessRuleException("Inactive customers cannot receive bills");
        }

        if (meter.getStatus() != StatusType.ACTIVE) {
            throw new BusinessRuleException("Meter must be ACTIVE to generate a bill");
        }

        if (!reading.getReadingMonth().equals(request.getBillingMonth())
                || !reading.getReadingYear().equals(request.getBillingYear())) {
            throw new BusinessRuleException("Billing period must match meter reading period");
        }

        if (billRepository.existsByMeterIdAndBillingMonthAndBillingYear(
                meter.getId(), request.getBillingMonth(), request.getBillingYear())) {
            throw new DuplicateResourceException("Bill already exists for this meter and billing period");
        }

        LocalDate billingDate = LocalDate.of(request.getBillingYear(), request.getBillingMonth(), 1);
        Tariff tariff = tariffService.findEffectiveTariff(meter.getMeterType(), billingDate);

        BigDecimal consumption = reading.getCurrentReading().subtract(reading.getPreviousReading());
        if (consumption.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Consumption cannot be negative. Check meter readings.");
        }

        BigDecimal consumptionAmount = tariffCalculationService.calculateConsumptionAmount(tariff, consumption);
        BigDecimal flatAmount = tariff.getFlatRate() != null ? consumptionAmount : BigDecimal.ZERO;
        BigDecimal tierAmount = tariff.getFlatRate() == null ? consumptionAmount : BigDecimal.ZERO;
        BigDecimal serviceCharge = tariff.getFixedServiceCharge();
        BigDecimal subtotal = consumptionAmount.add(serviceCharge);
        BigDecimal vatAmount = tariffCalculationService.calculateVat(subtotal, tariff.getVatPercentage());
        BigDecimal totalAmount = subtotal.add(vatAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Bill total amount cannot be negative");
        }

        Bill bill = Bill.builder()
                .customer(meter.getCustomer())
                .meter(meter)
                .tariff(tariff)
                .billingMonth(request.getBillingMonth())
                .billingYear(request.getBillingYear())
                .consumption(consumption)
                .flatAmount(flatAmount)
                .tierAmount(tierAmount)
                .serviceCharge(serviceCharge)
                .vatAmount(vatAmount)
                .penaltyAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .paidAmount(BigDecimal.ZERO)
                .outstandingBalance(totalAmount)
                .status(BillStatus.PENDING)
                .dueDate(billingDate.plusDays(30))
                .build();

        Bill saved = billRepository.save(bill);
        log.info("Bill generated for meter {} - {}/{}, total: {} (pending approval)",
                meter.getMeterNumber(), request.getBillingMonth(), request.getBillingYear(), totalAmount);

        var customer = meter.getCustomer();
        String message = NotificationService.buildBillGeneratedMessage(
                customer.getFullName(),
                request.getBillingMonth(),
                request.getBillingYear(),
                totalAmount);

        log.info("Bill generation notification for {} ({}):\n{}", customer.getFullName(), customer.getEmail(), message);

        if (customer.getUser() != null) {
            notificationService.createNotification(
                    customer.getUser(),
                    "Utility Bill Generated",
                    message,
                    NotificationType.BILL_GENERATED);
        }

        emailService.sendCustomerNotificationEmail(customer, "Utility bill generated - Utility Billing", message);

        return EntityMapper.toBillResponse(saved);
    }

    @Transactional
    /** Approves a pending bill and notifies the customer. */
    public BillResponse approveBill(Long id) {
        Bill bill = findBill(id);

        if (bill.getStatus() != BillStatus.PENDING) {
            throw new BusinessRuleException("Only pending bills can be approved");
        }

        bill.setStatus(BillStatus.APPROVED);

        var customer = bill.getCustomer();
        String message = NotificationService.buildBillApprovedMessage(
                customer.getFullName(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                bill.getTotalAmount());

        log.info("Bill approval notification for {} ({}):\n{}", customer.getFullName(), customer.getEmail(), message);

        if (customer.getUser() != null) {
            notificationService.createNotification(
                    customer.getUser(),
                    "Utility Bill Approved",
                    message,
                    NotificationType.BILL_APPROVED);
        }

        emailService.sendCustomerNotificationEmail(customer, "Utility bill approved - Utility Billing", message);

        log.info("Bill {} approved", bill.getId());
        return EntityMapper.toBillResponse(bill);
    }

    @Transactional
    /** Manually overrides bill status (e.g. mark CANCELLED). */
    public BillResponse updateBillStatus(Long id, BillStatus status) {
        Bill bill = findBill(id);
        bill.setStatus(status);
        log.info("Bill {} status updated to {}", bill.getId(), status);
        return EntityMapper.toBillResponse(bill);
    }

    @Transactional
    /** Adds a late-payment penalty to an overdue bill based on tariff penalty rate. */
    public void applyPenalty(Long billId) {
        Bill bill = findBill(billId);
        if (bill.getStatus() == BillStatus.PAID || bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal penalty = tariffCalculationService.calculatePenalty(
                bill.getOutstandingBalance(),
                bill.getTariff().getLatePaymentPenaltyPercentage());

        bill.setPenaltyAmount(bill.getPenaltyAmount().add(penalty));
        bill.setTotalAmount(bill.getTotalAmount().add(penalty));
        bill.setOutstandingBalance(bill.getOutstandingBalance().add(penalty));
        if (bill.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Bill total amount cannot be negative after penalty");
        }
        bill.setStatus(BillStatus.OVERDUE);
        log.info("Penalty applied to bill {}: {}", bill.getId(), penalty);
    }

    /** Loads a bill entity or throws ResourceNotFoundException. */
    public Bill findBill(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }
}
