package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.model.VirtualMachine;
import com.cms.cloudmanagement.repository.VirtualMachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VMService {

    @Autowired
    private VirtualMachineRepository vmRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BillingService billingService;

    public List<VirtualMachine> getAllVMs() {
        return vmRepository.findAll();
    }

    public List<VirtualMachine> getVMsForUser(User user) {
        return vmRepository.findByUser(user);
    }

    public VirtualMachine getVMById(Long id) {
        return vmRepository.findById(id).orElse(null);
    }

    public VirtualMachine createVM(VirtualMachine vm) {
        // Allocate resources in inventory
        inventoryService.allocateResources("CPU", vm.getCpuCores());
        inventoryService.allocateResources("RAM", vm.getRamGb());
        inventoryService.allocateResources("Storage", vm.getStorageGb());
        if (vm.getGpuCards() != null && vm.getGpuCards() > 0) {
            inventoryService.allocateResources("GPU", vm.getGpuCards());
        }
        if (vm.getBandwidthMbps() != null && vm.getBandwidthMbps() > 0) {
            inventoryService.allocateResources("Bandwidth", vm.getBandwidthMbps());
        }

        vm.setStatus("Running");
        VirtualMachine saved = vmRepository.save(vm);

        notificationService.createNotification(vm.getUser(),
                "Your Virtual Machine '" + vm.getName() + "' is provisioned and running.",
                "VM_CREATED");

        billingService.generateInvoiceForVM(vm.getUser(), saved);

        return saved;
    }

    public VirtualMachine updateStatus(Long id, String status) {
        VirtualMachine vm = vmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VM not found"));

        String oldStatus = vm.getStatus();
        vm.setStatus(status);
        VirtualMachine saved = vmRepository.save(vm);

        notificationService.createNotification(vm.getUser(),
                "Virtual Machine '" + vm.getName() + "' status changed from " + oldStatus + " to " + status + ".",
                "INFO");

        return saved;
    }

    public VirtualMachine upgradeVM(Long id, Integer cpu, Integer ram, Integer storage, Integer gpu, Integer bandwidth) {
        VirtualMachine vm = vmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VM not found"));

        // Release old resources temporarily to evaluate check
        inventoryService.releaseResources("CPU", vm.getCpuCores());
        inventoryService.releaseResources("RAM", vm.getRamGb());
        inventoryService.releaseResources("Storage", vm.getStorageGb());
        if (vm.getGpuCards() != null && vm.getGpuCards() > 0) {
            inventoryService.releaseResources("GPU", vm.getGpuCards());
        }
        if (vm.getBandwidthMbps() != null && vm.getBandwidthMbps() > 0) {
            inventoryService.releaseResources("Bandwidth", vm.getBandwidthMbps());
        }

        // Validate
        boolean cpuAvail = inventoryService.checkAvailability("CPU", cpu);
        boolean ramAvail = inventoryService.checkAvailability("RAM", ram);
        boolean storageAvail = inventoryService.checkAvailability("Storage", storage);
        boolean gpuAvail = gpu == null || gpu == 0 || inventoryService.checkAvailability("GPU", gpu);
        boolean bandwidthAvail = bandwidth == null || bandwidth == 0 || inventoryService.checkAvailability("Bandwidth", bandwidth);

        if (cpuAvail && ramAvail && storageAvail && gpuAvail && bandwidthAvail) {
            inventoryService.allocateResources("CPU", cpu);
            inventoryService.allocateResources("RAM", ram);
            inventoryService.allocateResources("Storage", storage);
            if (gpu != null && gpu > 0) inventoryService.allocateResources("GPU", gpu);
            if (bandwidth != null && bandwidth > 0) inventoryService.allocateResources("Bandwidth", bandwidth);

            vm.setCpuCores(cpu);
            vm.setRamGb(ram);
            vm.setStorageGb(storage);
            vm.setGpuCards(gpu);
            vm.setBandwidthMbps(bandwidth);

            VirtualMachine saved = vmRepository.save(vm);

            notificationService.createNotification(vm.getUser(),
                    "Virtual Machine '" + vm.getName() + "' has been successfully upgraded.",
                    "RESOURCE_UPGRADED");

            billingService.generateInvoiceForVM(vm.getUser(), saved);

            return saved;
        } else {
            // Re-allocate old resources
            inventoryService.allocateResources("CPU", vm.getCpuCores());
            inventoryService.allocateResources("RAM", vm.getRamGb());
            inventoryService.allocateResources("Storage", vm.getStorageGb());
            if (vm.getGpuCards() != null && vm.getGpuCards() > 0) {
                inventoryService.allocateResources("GPU", vm.getGpuCards());
            }
            if (vm.getBandwidthMbps() != null && vm.getBandwidthMbps() > 0) {
                inventoryService.allocateResources("Bandwidth", vm.getBandwidthMbps());
            }
            throw new RuntimeException("Upgrade failed: Insufficient resources in inventory");
        }
    }

    public void deleteVM(Long id) {
        VirtualMachine vm = vmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VM not found"));

        inventoryService.releaseResources("CPU", vm.getCpuCores());
        inventoryService.releaseResources("RAM", vm.getRamGb());
        inventoryService.releaseResources("Storage", vm.getStorageGb());
        if (vm.getGpuCards() != null && vm.getGpuCards() > 0) {
            inventoryService.releaseResources("GPU", vm.getGpuCards());
        }
        if (vm.getBandwidthMbps() != null && vm.getBandwidthMbps() > 0) {
            inventoryService.releaseResources("Bandwidth", vm.getBandwidthMbps());
        }

        vmRepository.delete(vm);

        notificationService.createNotification(vm.getUser(),
                "Virtual Machine '" + vm.getName() + "' was successfully terminated.",
                "VM_DELETED");
    }
}
