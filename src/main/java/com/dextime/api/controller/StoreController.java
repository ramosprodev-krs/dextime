package com.dextime.api.controller;

import com.dextime.api.dto.StoreSnapshotDto;
import com.dextime.api.entity.CustomerSnapshot;
import com.dextime.api.entity.ProviderSnapshot;
import com.dextime.api.entity.StoreSnapshot;
import com.dextime.api.exception.BusinessException;
import com.dextime.api.repository.CustomerSnapshotRepository;
import com.dextime.api.repository.ProviderSnapshotRepository;
import com.dextime.api.service.RabbitMQPublisherService;
import com.dextime.api.service.StoreSnapshotService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreSnapshotService storeSnapshotService;
    private final RabbitMQPublisherService rabbitMQPublisherService;
    private final CustomerSnapshotRepository customerSnapshotRepository;
    private final ProviderSnapshotRepository providerSnapshotRepository;

    public StoreController(StoreSnapshotService storeSnapshotService, 
                          RabbitMQPublisherService rabbitMQPublisherService,
                          CustomerSnapshotRepository customerSnapshotRepository,
                          ProviderSnapshotRepository providerSnapshotRepository) {
        this.storeSnapshotService = storeSnapshotService;
        this.rabbitMQPublisherService = rabbitMQPublisherService;
        this.customerSnapshotRepository = customerSnapshotRepository;
        this.providerSnapshotRepository = providerSnapshotRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public StoreSnapshot createStore(@RequestBody StoreSnapshotDto storeSnapshotDto, Authentication authentication) {
        String email = authentication.getName();
        CustomerSnapshot customer = customerSnapshotRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        ProviderSnapshot provider = providerSnapshotRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new BusinessException("Provider profile not found"));
        
        StoreSnapshot store = storeSnapshotService.createStoreSnapshot(storeSnapshotDto, provider.getId());
        rabbitMQPublisherService.publishMessage("store", "create", store.getId(), storeSnapshotDto);
        return store;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public List<StoreSnapshot> getAllStoreSnapshots() {
        return storeSnapshotService.getAllStoreSnapshots();
    }

    @GetMapping("/my-stores")
    @PreAuthorize("hasRole('PROVIDER')")
    public List<StoreSnapshot> getMyStores(Authentication authentication) {
        String email = authentication.getName();
        CustomerSnapshot customer = customerSnapshotRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        ProviderSnapshot provider = providerSnapshotRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new BusinessException("Provider profile not found"));
        
        return storeSnapshotService.getStoreSnapshotsByProviderId(provider.getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVIDER')")
    public StoreSnapshot getStoreSnapshotById(@PathVariable String id, Authentication authentication) {
        StoreSnapshot store = storeSnapshotService.getStoreSnapshotById(id);
        
        if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            String email = authentication.getName();
            CustomerSnapshot customer = customerSnapshotRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("User not found"));
            
            ProviderSnapshot provider = providerSnapshotRepository.findByUserId(customer.getId())
                    .orElseThrow(() -> new BusinessException("Provider profile not found"));
            
            if (!store.getProviderId().equals(provider.getId())) {
                throw new BusinessException("Access denied: You can only view your own stores");
            }
        }
        
        return store;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public StoreSnapshot updateStoreSnapshotById(@PathVariable String id, @RequestBody StoreSnapshotDto storeSnapshotDto, Authentication authentication) {
        StoreSnapshot existingStore = storeSnapshotService.getStoreSnapshotById(id);
        
        String email = authentication.getName();
        CustomerSnapshot customer = customerSnapshotRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        ProviderSnapshot provider = providerSnapshotRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new BusinessException("Provider profile not found"));
        
        if (!existingStore.getProviderId().equals(provider.getId())) {
            throw new BusinessException("Access denied: You can only update your own stores");
        }
        
        StoreSnapshot store = storeSnapshotService.updateStoreSnapshotById(id, storeSnapshotDto);
        rabbitMQPublisherService.publishMessage("store", "update", id, storeSnapshotDto);
        return store;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public void deleteStoreSnapshotById(@PathVariable String id, Authentication authentication) {
        StoreSnapshot existingStore = storeSnapshotService.getStoreSnapshotById(id);
        
        String email = authentication.getName();
        CustomerSnapshot customer = customerSnapshotRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        ProviderSnapshot provider = providerSnapshotRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new BusinessException("Provider profile not found"));
        
        if (!existingStore.getProviderId().equals(provider.getId())) {
            throw new BusinessException("Access denied: You can only delete your own stores");
        }
        
        storeSnapshotService.deleteStoreSnapshotById(id);
        rabbitMQPublisherService.publishMessage("store", "delete", id, null);
    }
}
