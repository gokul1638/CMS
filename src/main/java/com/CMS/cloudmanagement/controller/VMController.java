package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.model.VirtualMachine;
import com.cms.cloudmanagement.repository.UserRepository;
import com.cms.cloudmanagement.service.VMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vms")
@CrossOrigin(origins = "*")
public class VMController {

    @Autowired
    private VMService vmService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping
    public List<VirtualMachine> getVMs() {
        User user = getCurrentUser();
        if (user != null && ("ROLE_ADMIN".equalsIgnoreCase(user.getRole()) || 
                             "ROLE_SUPER_ADMIN".equalsIgnoreCase(user.getRole()) ||
                             "ROLE_SUPPORT".equalsIgnoreCase(user.getRole()))) {
            return vmService.getAllVMs();
        }
        return vmService.getVMsForUser(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VirtualMachine> getVMById(@PathVariable Long id) {
        VirtualMachine vm = vmService.getVMById(id);
        if (vm == null) {
            return ResponseEntity.notFound().build();
        }
        User user = getCurrentUser();
        if (user != null && !"ROLE_ADMIN".equalsIgnoreCase(user.getRole()) && 
                            !"ROLE_SUPER_ADMIN".equalsIgnoreCase(user.getRole()) &&
                            !"ROLE_SUPPORT".equalsIgnoreCase(user.getRole()) &&
                            !vm.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        return ResponseEntity.ok(vm);
    }

    @PostMapping
    public ResponseEntity<?> createVMDirectly(@RequestBody VirtualMachine vm) {
        try {
            User user = getCurrentUser();
            vm.setUser(user);
            VirtualMachine created = vmService.createVM(vm);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startVM(@PathVariable Long id) {
        try {
            VirtualMachine updated = vmService.updateStatus(id, "Running");
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<?> stopVM(@PathVariable Long id) {
        try {
            VirtualMachine updated = vmService.updateStatus(id, "Stopped");
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<?> restartVM(@PathVariable Long id) {
        try {
            vmService.updateStatus(id, "Restarting");
            VirtualMachine updated = vmService.updateStatus(id, "Running");
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/upgrade")
    public ResponseEntity<?> upgradeVM(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Integer cpu = Integer.valueOf(request.get("cpuCores").toString());
            Integer ram = Integer.valueOf(request.get("ramGb").toString());
            Integer storage = Integer.valueOf(request.get("storageGb").toString());
            Integer gpu = request.get("gpuCards") != null ? Integer.valueOf(request.get("gpuCards").toString()) : 0;
            Integer bandwidth = request.get("bandwidthMbps") != null ? Integer.valueOf(request.get("bandwidthMbps").toString()) : 0;

            VirtualMachine updated = vmService.upgradeVM(id, cpu, ram, storage, gpu, bandwidth);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVM(@PathVariable Long id) {
        try {
            vmService.deleteVM(id);
            return ResponseEntity.ok(Map.of("message", "Virtual Machine has been successfully terminated and resources returned to inventory"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
