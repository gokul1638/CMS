package com.cms.cloudmanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class HealthController {
    
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheckJson() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Cloud Management System");
        response.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheckText() {
        return new ResponseEntity<>("Cloud Management System is running", HttpStatus.OK);
    }
}
