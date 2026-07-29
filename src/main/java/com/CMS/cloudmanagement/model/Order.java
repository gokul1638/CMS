package com.cms.cloudmanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String vmName;
    private String operatingSystem;
    private Integer cpuCores;
    private Integer ramGb;
    private Integer storageGb;
    private Integer gpuCards;
    private Integer bandwidthMbps;
    private String region;
    
    private String status; // Draft, Submitted, Pending Approval, Approved, Provisioning, Active, Cancelled, Completed
    private LocalDateTime createdAt;

    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = "Draft";
    }

    public Order(User user, String vmName, String operatingSystem, Integer cpuCores, Integer ramGb, Integer storageGb, Integer gpuCards, Integer bandwidthMbps, String region) {
        this();
        this.user = user;
        this.vmName = vmName;
        this.operatingSystem = operatingSystem;
        this.cpuCores = cpuCores;
        this.ramGb = ramGb;
        this.storageGb = storageGb;
        this.gpuCards = gpuCards;
        this.bandwidthMbps = bandwidthMbps;
        this.region = region;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Integer getRamGb() {
        return ramGb;
    }

    public void setRamGb(Integer ramGb) {
        this.ramGb = ramGb;
    }

    public Integer getStorageGb() {
        return storageGb;
    }

    public void setStorageGb(Integer storageGb) {
        this.storageGb = storageGb;
    }

    public Integer getGpuCards() {
        return gpuCards;
    }

    public void setGpuCards(Integer gpuCards) {
        this.gpuCards = gpuCards;
    }

    public Integer getBandwidthMbps() {
        return bandwidthMbps;
    }

    public void setBandwidthMbps(Integer bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
