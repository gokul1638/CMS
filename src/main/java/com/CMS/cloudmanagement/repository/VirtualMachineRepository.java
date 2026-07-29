package com.cms.cloudmanagement.repository;

import com.cms.cloudmanagement.model.User;
import com.cms.cloudmanagement.model.VirtualMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VirtualMachineRepository extends JpaRepository<VirtualMachine, Long> {
    List<VirtualMachine> findByUser(User user);
    List<VirtualMachine> findByStatus(String status);
    long countByStatus(String status);
}
