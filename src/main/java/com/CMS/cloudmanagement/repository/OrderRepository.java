package com.cms.cloudmanagement.repository;

import com.cms.cloudmanagement.model.Order;
import com.cms.cloudmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByStatus(String status);
    long countByStatus(String status);
}
