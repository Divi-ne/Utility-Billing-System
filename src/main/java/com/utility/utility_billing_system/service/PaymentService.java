package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.dto.payment.PaymentRequest;
import com.utility.utility_billing_system.dto.payment.PaymentResponse;
import com.utility.utility_billing_system.entity.Bill;
import com.utility.utility_billing_system.entity.Payment;
import com.utility.utility_billing_system.enums.BillStatus;
import com.utility.utility_billing_system.enums.NotificationType;
import com.utility.utility_billing_system.enums.PaymentType;
import com.utility.utility_billing_system.exception.BusinessRuleException;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.mapper.EntityMapper;
import com.utility.utility_billing_system.repository.PaymentRepository;
import com.utility.utility_billing_system.security.CustomerAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for processing bill payments.
 * <p>
 * Validates payment amounts against outstanding balances, updates bill status
 * (PARTIALLY_PAID or PAID), and notifies the customer when a payment is recorded.
 */
@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillService billService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final CustomerAccessService customerAccessService;

    public PaymentService(
            PaymentRepository paymentRepository,
            BillService billService,
            NotificationService notificationService,
            EmailService emailService,
            CustomerAccessService customerAccessService) {
        this.paymentRepository = paymentRepository;
        this.billService = billService;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.customerAccessService = customerAccessService;
    }

    @Transactional(readOnly = true)
    /** Lists all payments in the system for staff reporting. */
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAllOrderByPaymentDateDesc().stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Lists all payments for a customer. */
    public List<PaymentResponse> getPaymentsByCustomer(Long customerId) {
        customerAccessService.assertOwnCustomer(customerId);
        return paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Lists all payments recorded against a specific bill. */
    public List<PaymentResponse> getPaymentsByBill(Long billId) {
        Bill bill = billService.findBill(billId);
        customerAccessService.assertOwnBill(bill);
        return paymentRepository.findByBillId(billId).stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Retrieves a payment by ID including remaining bill balance. */
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        customerAccessService.assertOwnPayment(payment);
        return toPaymentResponse(payment);
    }

    @Transactional
    /** Records a payment, updates bill balances/status, and sends a notification. */
    public PaymentResponse processPayment(PaymentRequest request) {
        Bill bill = billService.findBill(request.getBillId());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero");
        }

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessRuleException("Bill is already fully paid");
        }

        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot process payment for a cancelled bill");
        }

        if (bill.getStatus() == BillStatus.PENDING) {
            throw new BusinessRuleException("Bill must be approved before payment can be recorded");
        }

        if (request.getAmount().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BusinessRuleException("Payment amount exceeds outstanding balance");
        }

        PaymentType paymentType = request.getAmount().compareTo(bill.getOutstandingBalance()) == 0
                ? PaymentType.FULL
                : PaymentType.PARTIAL;

        Payment payment = Payment.builder()
                .bill(bill)
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .paymentType(paymentType)
                .reference(request.getReference())
                .build();

        Payment saved = paymentRepository.save(payment);
        updateBillAfterPayment(bill, request.getAmount(), paymentType);

        log.info("Payment processed for bill {}: {} ({})", bill.getId(), request.getAmount(), paymentType);

        var customer = bill.getCustomer();
        String message = NotificationService.buildBillProcessedMessage(
                customer.getFullName(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                bill.getTotalAmount());

        log.info("Payment confirmation for {} ({}):\n{}", customer.getFullName(), customer.getEmail(), message);

        if (customer.getUser() != null) {
            notificationService.createNotification(
                    customer.getUser(),
                    "Payment Processed",
                    message,
                    NotificationType.PAYMENT_RECEIVED);
        }

        emailService.sendPaymentConfirmationEmail(customer, message);

        return EntityMapper.toPaymentResponse(saved, bill.getOutstandingBalance());
    }

    private void updateBillAfterPayment(Bill bill, BigDecimal amount, PaymentType paymentType) {
        BigDecimal newPaidAmount = bill.getPaidAmount().add(amount);
        BigDecimal newOutstanding = bill.getOutstandingBalance().subtract(amount);

        bill.setPaidAmount(newPaidAmount);
        bill.setOutstandingBalance(newOutstanding.max(BigDecimal.ZERO));

        if (paymentType == PaymentType.FULL || newOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
            bill.setStatus(BillStatus.PAID);
            bill.setOutstandingBalance(BigDecimal.ZERO);
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return EntityMapper.toPaymentResponse(payment, payment.getBill().getOutstandingBalance());
    }
}
