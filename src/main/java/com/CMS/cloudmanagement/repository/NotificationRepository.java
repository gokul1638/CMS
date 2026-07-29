package com.cms.cloudmanagement.repository;

import com.cms.cloudmanagement.model.Notification;
import com.cms.cloudmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndReadStatusOrderByCreatedAtDesc(User user, String readStatus);
}
