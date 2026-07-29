package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.Order;
import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.repository.UserRepository;
import com.cms.cloudmanagement.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping
    public List<Order> getOrders() {
        User user = getCurrentUser();
        if (user != null && ("ROLE_ADMIN".equalsIgnoreCase(user.getRole()) || 
                             "ROLE_SUPER_ADMIN".equalsIgnoreCase(user.getRole()))) {
            return orderService.getAllOrders();
        }
        return orderService.getOrdersForUser(user);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            User user = getCurrentUser();
            order.setUser(user);
            Order created = orderService.createOrder(order);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveOrder(@PathVariable Long id) {
        try {
            Order approved = orderService.approveOrder(id);
            return ResponseEntity.ok(approved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectOrder(@PathVariable Long id) {
        try {
            Order rejected = orderService.rejectOrder(id);
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
