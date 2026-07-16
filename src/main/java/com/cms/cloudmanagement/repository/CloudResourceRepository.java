package com.cms.cloudmanagement.repository;

import com.cms.cloudmanagement.model.CloudResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CloudResourceRepository extends JpaRepository<CloudResource, Long> {
    List<CloudResource> findByStatus(String status);
    List<CloudResource> findByType(String type);
    List<CloudResource> findByRegion(String region);
    List<CloudResource> findByNameContainingIgnoreCase(String name);
}
