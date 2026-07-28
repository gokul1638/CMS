package com.cms.cloudmanagement.repository;

import com.cms.cloudmanagement.model.CloudResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudResourceRepository extends JpaRepository<CloudResource, Long> {
}
