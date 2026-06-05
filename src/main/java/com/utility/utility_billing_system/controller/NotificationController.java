package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.dto.notification.NotificationResponse;
import com.utility.utility_billing_system.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for in-app user notifications.
 * <p>
 * Notifications are created automatically when bills are generated or payments
 * are received. All authenticated roles can view and mark notifications as read.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Lists all notifications for a user, newest first. All authenticated roles. */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "Get all notifications for a user")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(wrap(notificationService.getNotificationsByUser(userId), "Notifications retrieved"));
    }

    /** Lists only unread notifications for a user. All authenticated roles. */
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "Get unread notifications for a user")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(wrap(notificationService.getUnreadNotifications(userId), "Unread notifications retrieved"));
    }

    /** Marks a single notification as read. All authenticated roles. */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(notificationService.markAsRead(id), "Notification marked as read"));
    }

    private <T> ApiResponse<T> wrap(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}
