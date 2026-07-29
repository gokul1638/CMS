package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.Tariff;
import com.cms.cloudmanagement.service.TariffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tariffs")
@CrossOrigin(origins = "*")
public class TariffController {

    @Autowired
    private TariffService tariffService;

    @GetMapping
    public List<Tariff> getAllTariffs() {
        return tariffService.getAllTariffs();
    }

    @GetMapping("/active")
    public List<Tariff> getActiveTariffs() {
        return tariffService.getActiveTariffs();
    }

    @PostMapping
    public ResponseEntity<?> updateTariff(@RequestBody Map<String, Object> request) {
        try {
            String resourceType = (String) request.get("resourceType");
            Double price = Double.valueOf(request.get("pricePerUnit").toString());
            Tariff updated = tariffService.updateTariff(resourceType, price);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
