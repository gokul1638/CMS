package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.CloudResource;
import com.cms.cloudmanagement.repository.CloudResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CloudResourceService {

    private final CloudResourceRepository repository;

    public CloudResourceService(CloudResourceRepository repository) {
        this.repository = repository;
    }

    public List<CloudResource> getAllResources() {
        return repository.findAll();
    }

    public CloudResource createResource(CloudResource resource) {
        return repository.save(resource);
    }
}
