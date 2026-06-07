package com.utility.utility_billing_system.service;

import com.utility.utility_billing_system.dto.notification.NotificationResponse;
import com.utility.utility_billing_system.entity.Notification;
import com.utility.utility_billing_system.entity.User;
import com.utility.utility_billing_system.enums.NotificationType;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.exception.UnauthorizedException;
import com.utility.utility_billing_system.mapper.EntityMapper;
import com.utility.utility_billing_system.repository.NotificationRepository;
import com.utility.utility_billing_system.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for creating and retrieving user notifications.
 * <p>
 * Notifications inform customers of billing events (new bills, payments received).
 * Also used internally by BillService and PaymentService after key operations.
 */
@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    /** Returns all notifications for the currently authenticated user. */
    public List<NotificationResponse> getMyNotifications() {
        return getNotificationsByUser(currentUserId());
    }

    @Transactional(readOnly = true)
    /** Returns unread notifications for the currently authenticated user. */
    public List<NotificationResponse> getMyUnreadNotifications() {
        return getUnreadNotifications(currentUserId());
    }

    @Transactional(readOnly = true)
    /** Returns all notifications for a user, sorted newest first. */
    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(EntityMapper::toNotificationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Returns only unread notifications for a user. */
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId).stream()
                .map(EntityMapper::toNotificationResponse)
                .toList();
    }

    @Transactional
    /** Sets a notification's read flag to true (only for the owning user). */
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (!notification.getUser().getId().equals(currentUserId())) {
            throw new UnauthorizedException("You can only mark your own notifications as read");
        }

        notification.setRead(true);
        return EntityMapper.toNotificationResponse(notification);
    }

    @Transactional
    /** Creates and persists a new notification (used internally by billing services). */
    public Notification createNotification(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created for user {}: {}", user.getEmail(), title);
        return saved;
    }

    /** Customer message when a new bill is generated. */
    public static String buildBillGeneratedMessage(
            String customerName, int billingMonth, int billingYear, BigDecimal amount) {
        return buildBillLifecycleMessage(customerName, billingMonth, billingYear, amount, "generated");
    }

    /** Customer message when a bill is approved for payment. */
    public static String buildBillApprovedMessage(
            String customerName, int billingMonth, int billingYear, BigDecimal amount) {
        return buildBillLifecycleMessage(customerName, billingMonth, billingYear, amount, "approved");
    }

    /** Customer message when a bill payment is recorded. */
    public static String buildBillProcessedMessage(
            String customerName, int billingMonth, int billingYear, BigDecimal amount) {
        return buildBillLifecycleMessage(customerName, billingMonth, billingYear, amount, "processed");
    }

    private static String buildBillLifecycleMessage(
            String customerName, int billingMonth, int billingYear, BigDecimal amount, String action) {
        return """
                Dear %s,
                Your %d/%d utility bill of %s FRW has been successfully %s."""
                .formatted(customerName, billingMonth, billingYear, amount, action)
                .strip();
    }

    private Long currentUserId() {
        return SecurityUtils.getCurrentUserDetails().getUser().getId();
    }
}
