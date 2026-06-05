package com.utility.utility_billing_system.mapper;

import com.utility.utility_billing_system.dto.bill.BillResponse;
import com.utility.utility_billing_system.dto.customer.CustomerResponse;
import com.utility.utility_billing_system.dto.meter.MeterResponse;
import com.utility.utility_billing_system.dto.notification.NotificationResponse;
import com.utility.utility_billing_system.dto.payment.PaymentResponse;
import com.utility.utility_billing_system.dto.reading.MeterReadingResponse;
import com.utility.utility_billing_system.dto.tariff.TariffResponse;
import com.utility.utility_billing_system.dto.tariff.TariffTierResponse;
import com.utility.utility_billing_system.dto.user.UserResponse;
import com.utility.utility_billing_system.entity.Bill;
import com.utility.utility_billing_system.entity.Customer;
import com.utility.utility_billing_system.entity.Meter;
import com.utility.utility_billing_system.entity.MeterReading;
import com.utility.utility_billing_system.entity.Notification;
import com.utility.utility_billing_system.entity.Payment;
import com.utility.utility_billing_system.entity.Tariff;
import com.utility.utility_billing_system.entity.TariffTier;
import com.utility.utility_billing_system.entity.User;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Static mapper converting JPA entities to API response DTOs.
 * <p>
 * Keeps controllers and services free of manual field mapping. Computes derived
 * values (e.g. consumption from readings, remaining balance on payments).
 */
public final class EntityMapper {

    private EntityMapper() {
    }

    /** Maps a User entity to UserResponse (excludes password). */
    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    /** Maps a Customer entity to CustomerResponse. */
    public static CustomerResponse toCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .nationalId(customer.getNationalId())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .status(customer.getStatus())
                .userId(customer.getUser() != null ? customer.getUser().getId() : null)
                .createdAt(customer.getCreatedAt())
                .build();
    }

    /** Maps a Meter entity to MeterResponse with customer info. */
    public static MeterResponse toMeterResponse(Meter meter) {
        return MeterResponse.builder()
                .id(meter.getId())
                .meterNumber(meter.getMeterNumber())
                .meterType(meter.getMeterType())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .customerId(meter.getCustomer().getId())
                .customerName(meter.getCustomer().getFullName())
                .createdAt(meter.getCreatedAt())
                .build();
    }

    /** Maps a MeterReading to response with computed consumption. */
    public static MeterReadingResponse toMeterReadingResponse(MeterReading reading) {
        BigDecimal consumption = reading.getCurrentReading().subtract(reading.getPreviousReading());
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .meterId(reading.getMeter().getId())
                .meterNumber(reading.getMeter().getMeterNumber())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .consumption(consumption)
                .readingDate(reading.getReadingDate())
                .readingMonth(reading.getReadingMonth())
                .readingYear(reading.getReadingYear())
                .createdAt(reading.getCreatedAt())
                .build();
    }

    /** Maps a Tariff entity including nested tier list. */
    public static TariffResponse toTariffResponse(Tariff tariff) {
        return TariffResponse.builder()
                .id(tariff.getId())
                .version(tariff.getVersion())
                .meterType(tariff.getMeterType())
                .flatRate(tariff.getFlatRate())
                .fixedServiceCharge(tariff.getFixedServiceCharge())
                .vatPercentage(tariff.getVatPercentage())
                .latePaymentPenaltyPercentage(tariff.getLatePaymentPenaltyPercentage())
                .effectiveFrom(tariff.getEffectiveFrom())
                .status(tariff.getStatus())
                .tiers(tariff.getTiers().stream()
                        .map(EntityMapper::toTariffTierResponse)
                        .collect(Collectors.toList()))
                .createdAt(tariff.getCreatedAt())
                .build();
    }

    public static TariffTierResponse toTariffTierResponse(TariffTier tier) {
        return TariffTierResponse.builder()
                .id(tier.getId())
                .minConsumption(tier.getMinConsumption())
                .maxConsumption(tier.getMaxConsumption())
                .rate(tier.getRate())
                .build();
    }

    /** Maps a Bill entity with full charge breakdown. */
    public static BillResponse toBillResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .customerId(bill.getCustomer().getId())
                .customerName(bill.getCustomer().getFullName())
                .meterId(bill.getMeter().getId())
                .meterNumber(bill.getMeter().getMeterNumber())
                .tariffId(bill.getTariff().getId())
                .tariffVersion(bill.getTariff().getVersion())
                .billingMonth(bill.getBillingMonth())
                .billingYear(bill.getBillingYear())
                .consumption(bill.getConsumption())
                .flatAmount(bill.getFlatAmount())
                .tierAmount(bill.getTierAmount())
                .serviceCharge(bill.getServiceCharge())
                .vatAmount(bill.getVatAmount())
                .penaltyAmount(bill.getPenaltyAmount())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .outstandingBalance(bill.getOutstandingBalance())
                .status(bill.getStatus())
                .dueDate(bill.getDueDate())
                .createdAt(bill.getCreatedAt())
                .build();
    }

    /** Maps a Payment entity including remaining bill balance. */
    public static PaymentResponse toPaymentResponse(Payment payment, BigDecimal remainingBalance) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .billId(payment.getBill().getId())
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType())
                .reference(payment.getReference())
                .paymentDate(payment.getPaymentDate())
                .remainingBalance(remainingBalance)
                .build();
    }

    /** Maps a Notification entity to NotificationResponse. */
    public static NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
