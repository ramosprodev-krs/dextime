package com.dextime.api.controller;

import com.dextime.api.dto.ProviderSnapshotDto;
import com.dextime.api.entity.CustomerSnapshot;
import com.dextime.api.entity.ProviderSnapshot;
import com.dextime.api.exception.BusinessException;
import com.dextime.api.repository.CustomerSnapshotRepository;
import com.dextime.api.repository.ProviderSnapshotRepository;
import com.dextime.api.service.ProviderSnapshotService;
import com.dextime.api.service.RabbitMQPublisherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    private final ProviderSnapshotService providerSnapshotService;
    private final RabbitMQPublisherService rabbitMQPublisherService;
    private final CustomerSnapshotRepository customerSnapshotRepository;
    private final ProviderSnapshotRepository providerSnapshotRepository;

    public ProviderController(ProviderSnapshotService providerSnapshotService, 
                            RabbitMQPublisherService rabbitMQPublisherService,
                            CustomerSnapshotRepository customerSnapshotRepository,
                            ProviderSnapshotRepository providerSnapshotRepository) {
        this.providerSnapshotService = providerSnapshotService;
        this.rabbitMQPublisherService = rabbitMQPublisherService;
        this.customerSnapshotRepository = customerSnapshotRepository;
        this.providerSnapshotRepository = providerSnapshotRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProviderSnapshot createProvider(@RequestBody ProviderSnapshotDto providerSnapshotDto) {
        ProviderSnapshot provider = providerSnapshotService.createProviderSnapshot(providerSnapshotDto);
        rabbitMQPublisherService.publishMessage("provider", "create", provider.getId(), providerSnapshotDto);
        return provider;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProviderSnapshot> getAllProviderSnapshots() {
        return providerSnapshotService.getAllProviderSnapshots();
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ProviderSnapshot getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        CustomerSnapshot customer = customerSnapshotRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        return providerSnapshotRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new BusinessException("Provider profile not found"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProviderSnapshot getProviderSnapshotById(@PathVariable String id) {
        return providerSnapshotService.getProviderSnapshotById(id);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ProviderSnapshot updateMyProfile(@RequestBody ProviderSnapshotDto providerSnapshotDto, Authentication authentication) {
        String email = authentication.getName();
        CustomerSnapshot customer = customerSnapshotRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        ProviderSnapshot provider = providerSnapshotRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new BusinessException("Provider profile not found"));
        
        ProviderSnapshot updatedProvider = providerSnapshotService.updateProviderSnapshotById(provider.getId(), providerSnapshotDto);
        rabbitMQPublisherService.publishMessage("provider", "update", provider.getId(), providerSnapshotDto);
        return updatedProvider;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProviderSnapshot updateProviderSnapshotById(@PathVariable String id, @RequestBody ProviderSnapshotDto providerSnapshotDto) {
        ProviderSnapshot provider = providerSnapshotService.updateProviderSnapshotById(id, providerSnapshotDto);
        rabbitMQPublisherService.publishMessage("provider", "update", id, providerSnapshotDto);
        return provider;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProviderSnapshotById(@PathVariable String id) {
        providerSnapshotService.deleteProviderSnapshotById(id);
        rabbitMQPublisherService.publishMessage("provider", "delete", id, null);
    }
}
