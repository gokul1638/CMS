package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.Tariff;
import com.cms.cloudmanagement.repository.TariffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TariffService {

    @Autowired
    private TariffRepository tariffRepository;

    public List<Tariff> getAllTariffs() {
        return tariffRepository.findAll();
    }

    public List<Tariff> getActiveTariffs() {
        return tariffRepository.findByStatus("ACTIVE");
    }

    public Double getPriceForResource(String resourceType) {
        return tariffRepository.findByResourceTypeAndStatus(resourceType, "ACTIVE")
                .map(Tariff::getPricePerUnit)
                .orElse(0.0);
    }

    public Tariff updateTariff(String resourceType, Double newPrice) {
        // Disable old active tariff
        Optional<Tariff> activeOpt = tariffRepository.findByResourceTypeAndStatus(resourceType, "ACTIVE");
        if (activeOpt.isPresent()) {
            Tariff active = activeOpt.get();
            active.setStatus("INACTIVE");
            active.setEffectiveTo(LocalDateTime.now());
            tariffRepository.save(active);
        }

        // Create new active tariff
        Tariff newTariff = new Tariff(resourceType, newPrice, LocalDateTime.now());
        return tariffRepository.save(newTariff);
    }
}
