package com.cms.cloudmanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tariffs")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resourceType; // CPU, RAM, Storage, GPU, Bandwidth
    private Double pricePerUnit;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String status; // ACTIVE, INACTIVE

    public Tariff() {
        this.effectiveFrom = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Tariff(String resourceType, Double pricePerUnit, LocalDateTime effectiveFrom) {
        this();
        this.resourceType = resourceType;
        this.pricePerUnit = pricePerUnit;
        this.effectiveFrom = effectiveFrom;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(Double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
