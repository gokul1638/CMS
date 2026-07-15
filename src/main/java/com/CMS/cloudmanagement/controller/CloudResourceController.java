package com.cms.cloudmanagement.controller;

import com.cms.cloudmanagement.model.CloudResource;
import com.cms.cloudmanagement.service.CloudResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class CloudResourceController {

    private final CloudResourceService service;

    public CloudResourceController(CloudResourceService service) {
        this.service = service;
    }

    @GetMapping
    public List<CloudResource> getAllResources() {
        return service.getAllResources();
    }

    @PostMapping
    public CloudResource createResource(@RequestBody CloudResource resource) {
        return service.createResource(resource);
    }
}
