package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.*;
import com.cms.cloudmanagement.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private VMService vmService;

    @Autowired
    private NotificationService notificationService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(Order order) {
        // Validate inventory limits
        boolean cpuAvail = inventoryService.checkAvailability("CPU", order.getCpuCores());
        boolean ramAvail = inventoryService.checkAvailability("RAM", order.getRamGb());
        boolean storageAvail = inventoryService.checkAvailability("Storage", order.getStorageGb());
        boolean gpuAvail = order.getGpuCards() == null || order.getGpuCards() == 0 || inventoryService.checkAvailability("GPU", order.getGpuCards());
        boolean bandwidthAvail = order.getBandwidthMbps() == null || order.getBandwidthMbps() == 0 || inventoryService.checkAvailability("Bandwidth", order.getBandwidthMbps());

        if (!cpuAvail || !ramAvail || !storageAvail || !gpuAvail || !bandwidthAvail) {
            throw new RuntimeException("Provisioning rejected: Insufficient resources available in inventory.");
        }

        order.setStatus("Pending Approval");
        Order saved = orderRepository.save(order);

        notificationService.createNotification(order.getUser(),
                "Your provisioning order for VM '" + order.getVmName() + "' was submitted and is pending administrator approval.",
                "INFO");

        return saved;
    }

    public Order approveOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"Pending Approval".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Only pending orders can be approved");
        }

        order.setStatus("Approved");
        orderRepository.save(order);

        // Transition from Approval to VM Creation
        VirtualMachine vm = new VirtualMachine(
                order.getVmName(),
                order.getOperatingSystem(),
                order.getCpuCores(),
                order.getRamGb(),
                order.getStorageGb(),
                order.getGpuCards(),
                order.getBandwidthMbps(),
                order.getRegion(),
                order.getUser()
        );
        vmService.createVM(vm);

        order.setStatus("Completed");
        return orderRepository.save(order);
    }

    public Order rejectOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"Pending Approval".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Only pending orders can be rejected");
        }

        order.setStatus("Cancelled");
        Order saved = orderRepository.save(order);

        notificationService.createNotification(order.getUser(),
                "Your provisioning request for VM '" + order.getVmName() + "' was rejected by the administrator.",
                "INFO");

        return saved;
    }
}
