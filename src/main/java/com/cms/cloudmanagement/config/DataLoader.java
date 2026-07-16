package com.cms.cloudmanagement.config;

import com.cms.cloudmanagement.model.CloudResource;
import com.cms.cloudmanagement.repository.CloudResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CloudResourceRepository resourceRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (resourceRepository.count() > 0) {
            return; // Data already loaded
        }

        // Insert sample data
        resourceRepository.save(new CloudResource("Production Web Server", "VM", "ACTIVE", 
            "Main production web server hosting the API", "us-east-1"));
        
        resourceRepository.save(new CloudResource("Database Server", "Database", "ACTIVE", 
            "Primary MySQL database for production", "us-east-1"));
        
        resourceRepository.save(new CloudResource("Backup Storage", "Storage", "ACTIVE", 
            "S3 backup storage for all configurations and data", "us-west-2"));
        
        resourceRepository.save(new CloudResource("Development VM", "VM", "INACTIVE", 
            "Development environment for testing new features", "us-east-1"));
        
        resourceRepository.save(new CloudResource("Cache Server", "Database", "ACTIVE", 
            "Redis cache for performance optimization", "us-east-1"));
        
        resourceRepository.save(new CloudResource("Log Storage", "Storage", "ACTIVE", 
            "Centralized logging and monitoring storage", "eu-west-1"));
        
        resourceRepository.save(new CloudResource("Load Balancer", "Network", "ACTIVE", 
            "Application load balancer for traffic distribution", "us-east-1"));
        
        resourceRepository.save(new CloudResource("Staging Database", "Database", "ACTIVE", 
            "Staging environment database for testing", "us-east-1"));
        
        resourceRepository.save(new CloudResource("CDN Distribution", "Network", "ACTIVE", 
            "CloudFront CDN for content delivery", "us-east-1"));
        
        resourceRepository.save(new CloudResource("Legacy System", "VM", "TERMINATED", 
            "Old legacy system no longer in use", "ap-south-1"));
        
        System.out.println("✓ Sample data loaded successfully!");
    }
}
