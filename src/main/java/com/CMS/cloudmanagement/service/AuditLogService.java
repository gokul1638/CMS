package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.AuditLog;
import com.cms.cloudmanagement.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String username, String action, String previousValue, String newValue, String ipAddress) {
        AuditLog auditLog = new AuditLog(username, action, previousValue, newValue, ipAddress);
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
