package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    private final Map<String, String> resetTokens = new HashMap<>();

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registered = authService.registerUser(user);
            return ResponseEntity.ok(registered);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String token = authService.login(username, password);
            
            User user = authService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("email", user.getEmail());
            response.put("subscriptionPlan", user.getSubscriptionPlan());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials or account disabled"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        User user = authService.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username not found"));
        }

        String token = UUID.randomUUID().toString().substring(0, 8);
        resetTokens.put(username, token);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reset code generated successfully.");
        response.put("resetCode", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        String savedCode = resetTokens.get(username);
        if (savedCode == null || !savedCode.equals(code)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid reset code"));
        }

        try {
            authService.updatePassword(username, newPassword);
            resetTokens.remove(username);
            return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
