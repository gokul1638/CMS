package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.CloudResource;
import com.cms.cloudmanagement.service.CloudResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class CloudResourceController {
    
    @Autowired
    private CloudResourceService resourceService;
    
    @GetMapping
    public ResponseEntity<List<CloudResource>> getAllResources() {
        List<CloudResource> resources = resourceService.getAllResources();
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CloudResource> getResourceById(@PathVariable Long id) {
        Optional<CloudResource> resource = resourceService.getResourceById(id);
        if (resource.isPresent()) {
            return new ResponseEntity<>(resource.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @PostMapping
    public ResponseEntity<CloudResource> createResource(@RequestBody CloudResource resource) {
        CloudResource created = resourceService.createResource(resource);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CloudResource> updateResource(@PathVariable Long id, @RequestBody CloudResource resourceDetails) {
        CloudResource updated = resourceService.updateResource(id, resourceDetails);
        if (updated != null) {
            return new ResponseEntity<>(updated, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        if (resourceService.deleteResource(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @GetMapping("/search/status")
    public ResponseEntity<List<CloudResource>> getResourcesByStatus(@RequestParam String status) {
        List<CloudResource> resources = resourceService.getResourcesByStatus(status);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
    
    @GetMapping("/search/type")
    public ResponseEntity<List<CloudResource>> getResourcesByType(@RequestParam String type) {
        List<CloudResource> resources = resourceService.getResourcesByType(type);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
    
    @GetMapping("/search/region")
    public ResponseEntity<List<CloudResource>> getResourcesByRegion(@RequestParam String region) {
        List<CloudResource> resources = resourceService.getResourcesByRegion(region);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
    
    @GetMapping("/search/name")
    public ResponseEntity<List<CloudResource>> searchByName(@RequestParam String name) {
        List<CloudResource> resources = resourceService.searchResourcesByName(name);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
}
