package com.cms.cloudmanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resourceType; // CPU, RAM, Storage, GPU, Bandwidth
    private Integer totalQuantity;
    private Integer allocatedQuantity;

    public Inventory() {
        this.allocatedQuantity = 0;
    }

    public Inventory(String resourceType, Integer totalQuantity, Integer allocatedQuantity) {
        this.resourceType = resourceType;
        this.totalQuantity = totalQuantity;
        this.allocatedQuantity = allocatedQuantity;
    }

    public Integer getAvailableQuantity() {
        return totalQuantity - allocatedQuantity;
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

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAllocatedQuantity() {
        return allocatedQuantity;
    }

    public void setAllocatedQuantity(Integer allocatedQuantity) {
        this.allocatedQuantity = allocatedQuantity;
    }
}
