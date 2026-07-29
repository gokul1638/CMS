package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.CloudResource;
import com.cms.cloudmanagement.repository.CloudResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class CloudResourceController {

    @Autowired
    private CloudResourceRepository resourceRepository;

    @GetMapping
    public List<CloudResource> getAllResources() {
        return resourceRepository.findAll();
    }

    @PostMapping
    public CloudResource createResource(@RequestBody CloudResource resource) {
        return resourceRepository.save(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CloudResource> getResourceById(@PathVariable Long id) {
        return resourceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CloudResource> updateResource(@PathVariable Long id, @RequestBody CloudResource resourceDetails) {
        return resourceRepository.findById(id)
                .map(resource -> {
                    resource.setName(resourceDetails.getName());
                    resource.setUnit(resourceDetails.getUnit());
                    resource.setDescription(resourceDetails.getDescription());
                    resource.setTariff(resourceDetails.getTariff());
                    resource.setAvailableQuantity(resourceDetails.getAvailableQuantity());
                    resource.setTotalQuantity(resourceDetails.getTotalQuantity());
                    resource.setStatus(resourceDetails.getStatus());
                    return ResponseEntity.ok(resourceRepository.save(resource));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        return resourceRepository.findById(id)
                .map(resource -> {
                    resourceRepository.delete(resource);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
