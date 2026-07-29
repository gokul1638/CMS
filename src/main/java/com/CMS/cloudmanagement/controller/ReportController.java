package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.*;
import com.cms.cloudmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VirtualMachineRepository vmRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public ResponseEntity<?> getReportsSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Generate report summaries for dashboard listing
        summary.put("totalRevenue", invoiceRepository.findByStatus("Paid").stream().mapToDouble(Invoice::getGrandTotal).sum());
        summary.put("registeredUsers", userRepository.count());
        summary.put("provisionedVMs", vmRepository.count());
        summary.put("activeSubscriptions", vmRepository.countByStatus("Running"));

        List<Map<String, String>> reportsList = List.of(
            Map.of("id", "revenue", "name", "Revenue Report", "description", "Summarizes total billing, pending collections, and paid revenue."),
            Map.of("id", "users", "name", "User Report", "description", "Catalog of registered cloud customers, their status, and assigned plans."),
            Map.of("id", "allocation", "name", "Resource Allocation", "description", "Lists allocated compute resources, Memory sizes, and Storage allocations per VM."),
            Map.of("id", "payments", "name", "Payment History", "description", "Logs all successful, failed, or refunded mock payments.")
        );
        summary.put("availableReports", reportsList);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{type}/export")
    public ResponseEntity<String> exportReport(@PathVariable String type) {
        StringBuilder csv = new StringBuilder();
        String filename = type + "_report.csv";

        if ("revenue".equalsIgnoreCase(type)) {
            csv.append("Invoice Number,Customer,Subtotal,GST,Discount,Grand Total,Status,Due Date,Generated Date\n");
            List<Invoice> invoices = invoiceRepository.findAll();
            for (Invoice inv : invoices) {
                csv.append(inv.getInvoiceNumber()).append(",")
                   .append(inv.getUser().getUsername()).append(",")
                   .append(inv.getSubtotal()).append(",")
                   .append(inv.getGst()).append(",")
                   .append(inv.getDiscount()).append(",")
                   .append(inv.getGrandTotal()).append(",")
                   .append(inv.getStatus()).append(",")
                   .append(inv.getDueDate()).append(",")
                   .append(inv.getCreatedAt()).append("\n");
            }
        } else if ("users".equalsIgnoreCase(type)) {
            csv.append("User ID,Username,Email,Role,Account Status,Subscription Plan,Created Date\n");
            List<User> users = userRepository.findAll();
            for (User u : users) {
                csv.append(u.getId()).append(",")
                   .append(u.getUsername()).append(",")
                   .append(u.getEmail()).append(",")
                   .append(u.getRole()).append(",")
                   .append(u.getStatus()).append(",")
                   .append(u.getSubscriptionPlan()).append(",")
                   .append(u.getCreatedAt()).append("\n");
            }
        } else if ("allocation".equalsIgnoreCase(type)) {
            csv.append("VM ID,VM Name,Owner,OS,Region,CPU Cores,RAM GB,Storage GB,GPU Cards,Bandwidth Mbps,Status,Created Date\n");
            List<VirtualMachine> vms = vmRepository.findAll();
            for (VirtualMachine vm : vms) {
                csv.append(vm.getId()).append(",")
                   .append(vm.getName()).append(",")
                   .append(vm.getUser().getUsername()).append(",")
                   .append(vm.getOperatingSystem()).append(",")
                   .append(vm.getRegion()).append(",")
                   .append(vm.getCpuCores()).append(",")
                   .append(vm.getRamGb()).append(",")
                   .append(vm.getStorageGb()).append(",")
                   .append(vm.getGpuCards()).append(",")
                   .append(vm.getBandwidthMbps()).append(",")
                   .append(vm.getStatus()).append(",")
                   .append(vm.getCreatedAt()).append("\n");
            }
        } else if ("payments".equalsIgnoreCase(type)) {
            csv.append("Payment ID,Invoice Number,Customer,Amount Paid,Method,Status,Date\n");
            List<Payment> payments = paymentRepository.findAll();
            for (Payment p : payments) {
                csv.append(p.getId()).append(",")
                   .append(p.getInvoice().getInvoiceNumber()).append(",")
                   .append(p.getUser().getUsername()).append(",")
                   .append(p.getAmount()).append(",")
                   .append(p.getMethod()).append(",")
                   .append(p.getStatus()).append(",")
                   .append(p.getPaymentDate()).append("\n");
            }
        } else {
            return ResponseEntity.badRequest().body("Unknown report type: " + type);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv.toString());
    }
}
