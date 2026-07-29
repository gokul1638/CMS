package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/me")
    public ResponseEntity<User> getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody User userDetails) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setEmail(userDetails.getEmail());
                    if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
                        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    }
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return userRepository.findById(id)
                .map(user -> {
                    if (updates.containsKey("role")) {
                        String role = (String) updates.get("role");
                        if (!role.startsWith("ROLE_")) {
                            role = "ROLE_" + role.toUpperCase();
                        }
                        user.setRole(role);
                    }
                    if (updates.containsKey("status")) {
                        user.setStatus((String) updates.get("status"));
                    }
                    if (updates.containsKey("subscriptionPlan")) {
                        user.setSubscriptionPlan((String) updates.get("subscriptionPlan"));
                    }
                    if (updates.containsKey("email")) {
                        user.setEmail((String) updates.get("email"));
                    }
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
