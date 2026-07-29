package com.cms.cloudmanagement.repository;

import com.cms.cloudmanagement.model.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    List<Tariff> findByStatus(String status);
    Optional<Tariff> findByResourceTypeAndStatus(String resourceType, String status);
}
