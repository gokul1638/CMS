package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.*;
import com.cms.cloudmanagement.repository.*;
import com.cms.cloudmanagement.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VirtualMachineRepository vmRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TariffRepository tariffRepository;

    @GetMapping
    public ResponseEntity<?> getDashboardData() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Map<String, Object> data = new HashMap<>();
        String role = user.getRole();

        if ("ROLE_ADMIN".equalsIgnoreCase(role) || 
            "ROLE_SUPER_ADMIN".equalsIgnoreCase(role) ||
            "ROLE_SUPPORT".equalsIgnoreCase(role)) {
            
            // ADMIN / STAFF DASHBOARD
            data.put("totalUsers", userRepository.count());
            data.put("totalVMs", vmRepository.count());
            data.put("runningVMs", vmRepository.countByStatus("Running"));
            data.put("pendingApprovals", orderRepository.countByStatus("Pending Approval"));

            // Calculate Monthly Revenue (sum of Paid Invoices)
            double revenue = invoiceRepository.findByStatus("Paid").stream()
                    .mapToDouble(Invoice::getGrandTotal)
                    .sum();
            data.put("monthlyRevenue", revenue);

            // Fetch Resource Inventory Utilization
            List<Inventory> inventoryList = inventoryRepository.findAll();
            data.put("inventory", inventoryList);

            // Active Tariffs
            List<Tariff> activeTariffs = tariffRepository.findByStatus("ACTIVE");
            data.put("tariffs", activeTariffs);

            // System Health Status
            data.put("systemHealth", "Healthy");
            data.put("uptime", "99.98%");
            
            // Revenue history for chart (mock values)
            data.put("revenueChart", Map.of(
                "labels", List.of("Feb", "Mar", "Apr", "May", "Jun", "Jul"),
                "values", List.of(revenue * 0.4, revenue * 0.6, revenue * 0.7, revenue * 0.8, revenue * 0.9, revenue)
            ));

        } else {
            // CUSTOMER DASHBOARD
            List<VirtualMachine> userVMs = vmRepository.findByUser(user);
            data.put("runningVMs", userVMs.stream().filter(vm -> "Running".equalsIgnoreCase(vm.getStatus())).count());
            data.put("totalVMs", userVMs.size());

            // Compute allocated metrics
            int totalCpu = userVMs.stream().mapToInt(VirtualMachine::getCpuCores).sum();
            int totalRam = userVMs.stream().mapToInt(VirtualMachine::getRamGb).sum();
            int totalStorage = userVMs.stream().mapToInt(VirtualMachine::getStorageGb).sum();

            data.put("totalCpu", totalCpu);
            data.put("totalRam", totalRam);
            data.put("totalStorage", totalStorage);
            data.put("subscriptionPlan", user.getSubscriptionPlan());

            // Compute pending invoices sum
            List<Invoice> userInvoices = invoiceRepository.findByUser(user);
            double pendingBillAmount = userInvoices.stream()
                    .filter(inv -> "Generated".equalsIgnoreCase(inv.getStatus()) || "Pending".equalsIgnoreCase(inv.getStatus()))
                    .mapToDouble(Invoice::getGrandTotal)
                    .sum();
            data.put("pendingBill", pendingBillAmount);

            // Notification list
            List<Notification> recentNotifications = notificationService.getNotificationsForUser(user);
            data.put("notifications", recentNotifications.subList(0, Math.min(recentNotifications.size(), 5)));

            // Simulated VM metrics for charts (CPU usage 0-100%, memory usage, network usage)
            List<Integer> cpuSeries = new ArrayList<>();
            List<Integer> memorySeries = new ArrayList<>();
            Random r = new Random();
            for (int i = 0; i < 7; i++) {
                cpuSeries.add(userVMs.isEmpty() ? 0 : 20 + r.nextInt(50));
                memorySeries.add(userVMs.isEmpty() ? 0 : 40 + r.nextInt(30));
            }
            data.put("cpuUsageChart", cpuSeries);
            data.put("memoryUsageChart", memorySeries);
        }

        return ResponseEntity.ok(data);
    }
}
