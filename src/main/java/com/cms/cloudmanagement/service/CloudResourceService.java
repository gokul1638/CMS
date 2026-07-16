package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.CloudResource;
import com.cms.cloudmanagement.repository.CloudResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CloudResourceService {
    
    @Autowired
    private CloudResourceRepository repository;
    
    public List<CloudResource> getAllResources() {
        return repository.findAll();
    }
    
    public Optional<CloudResource> getResourceById(Long id) {
        return repository.findById(id);
    }
    
    public CloudResource createResource(CloudResource resource) {
        return repository.save(resource);
    }
    
    public CloudResource updateResource(Long id, CloudResource resourceDetails) {
        Optional<CloudResource> resource = repository.findById(id);
        if (resource.isPresent()) {
            CloudResource existing = resource.get();
            if (resourceDetails.getName() != null) {
                existing.setName(resourceDetails.getName());
            }
            if (resourceDetails.getType() != null) {
                existing.setType(resourceDetails.getType());
            }
            if (resourceDetails.getStatus() != null) {
                existing.setStatus(resourceDetails.getStatus());
            }
            if (resourceDetails.getDescription() != null) {
                existing.setDescription(resourceDetails.getDescription());
            }
            if (resourceDetails.getRegion() != null) {
                existing.setRegion(resourceDetails.getRegion());
            }
            return repository.save(existing);
        }
        return null;
    }
    
    public boolean deleteResource(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<CloudResource> getResourcesByStatus(String status) {
        return repository.findByStatus(status);
    }
    
    public List<CloudResource> getResourcesByType(String type) {
        return repository.findByType(type);
    }
    
    public List<CloudResource> getResourcesByRegion(String region) {
        return repository.findByRegion(region);
    }
    
    public List<CloudResource> searchResourcesByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }
}
