package com.cms.cloudmanagement.repository;

import com.cms.cloudmanagement.model.Payment;
import com.cms.cloudmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser(User user);
}
