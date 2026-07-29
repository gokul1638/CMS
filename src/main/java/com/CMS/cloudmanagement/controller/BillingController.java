package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.Invoice;
import com.cms.cloudmanagement.model.Payment;
import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.repository.UserRepository;
import com.cms.cloudmanagement.service.BillingService;
import com.cms.cloudmanagement.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private boolean isBillingPrivileged(User user) {
        if (user == null) return false;
        String role = user.getRole();
        return "ROLE_ADMIN".equalsIgnoreCase(role) || 
               "ROLE_SUPER_ADMIN".equalsIgnoreCase(role) || 
               "ROLE_BILLING_ADMIN".equalsIgnoreCase(role);
    }

    @GetMapping("/api/invoices")
    public List<Invoice> getInvoices() {
        User user = getCurrentUser();
        if (isBillingPrivileged(user)) {
            return billingService.getAllInvoices();
        }
        return billingService.getInvoicesForUser(user);
    }

    @GetMapping("/api/invoices/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        Invoice invoice = billingService.getInvoiceById(id);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        User user = getCurrentUser();
        if (user != null && !isBillingPrivileged(user) && !invoice.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/api/payments")
    public List<Payment> getPayments() {
        User user = getCurrentUser();
        if (isBillingPrivileged(user)) {
            return paymentService.getAllPayments();
        }
        return paymentService.getPaymentsForUser(user);
    }

    @PostMapping("/api/payments")
    public ResponseEntity<?> makePayment(@RequestBody Map<String, Object> request) {
        try {
            Long invoiceId = Long.valueOf(request.get("invoiceId").toString());
            String method = (String) request.get("method");
            boolean success = request.get("simulateSuccess") == null || (boolean) request.get("simulateSuccess");

            Payment processed = paymentService.processPayment(invoiceId, method, success);
            return ResponseEntity.ok(processed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/payments/{id}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable Long id) {
        try {
            User user = getCurrentUser();
            if (!isBillingPrivileged(user)) {
                return ResponseEntity.status(403).body(Map.of("message", "Only billing administrators can issue refunds"));
            }
            Payment refunded = paymentService.processRefund(id);
            return ResponseEntity.ok(refunded);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
