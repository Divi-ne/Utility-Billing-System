package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.Notification} entities.
 * <p>
 * Retrieves notifications by user, ordered by creation date, with unread filtering.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadFalse(Long userId);
}
