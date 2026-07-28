package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.CloudResource;
import com.cms.cloudmanagement.repository.CloudResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
