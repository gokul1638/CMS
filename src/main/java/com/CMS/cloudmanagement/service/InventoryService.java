package com.cms.cloudmanagement.service;

import com.cms.cloudmanagement.model.Inventory;
import com.cms.cloudmanagement.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public boolean checkAvailability(String resourceType, Integer requiredAmount) {
        Optional<Inventory> opt = inventoryRepository.findByResourceType(resourceType);
        if (opt.isPresent()) {
            Inventory inv = opt.get();
            return inv.getAvailableQuantity() >= requiredAmount;
        }
        return false;
    }

    public boolean allocateResources(String resourceType, Integer amount) {
        Optional<Inventory> opt = inventoryRepository.findByResourceType(resourceType);
        if (opt.isPresent()) {
            Inventory inv = opt.get();
            if (inv.getAvailableQuantity() >= amount) {
                inv.setAllocatedQuantity(inv.getAllocatedQuantity() + amount);
                inventoryRepository.save(inv);
                return true;
            }
        }
        return false;
    }

    public void releaseResources(String resourceType, Integer amount) {
        Optional<Inventory> opt = inventoryRepository.findByResourceType(resourceType);
        if (opt.isPresent()) {
            Inventory inv = opt.get();
            int newAllocated = Math.max(0, inv.getAllocatedQuantity() - amount);
            inv.setAllocatedQuantity(newAllocated);
            inventoryRepository.save(inv);
        }
    }

    public void updateLimit(String resourceType, Integer newLimit) {
        Inventory inv = inventoryRepository.findByResourceType(resourceType)
                .orElse(new Inventory(resourceType, newLimit, 0));
        inv.setTotalQuantity(newLimit);
        inventoryRepository.save(inv);
    }
}
