package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.AuditLog;
import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.repository.UserRepository;
import com.cms.cloudmanagement.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAuditLogs() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null || (!"ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole()) && 
                                    !"ROLE_SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied: Administrator privileges required"));
        }

        List<AuditLog> logs = auditLogService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
}
