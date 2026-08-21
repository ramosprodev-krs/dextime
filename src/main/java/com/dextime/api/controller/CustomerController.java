package com.dextime.api.controller;

import com.dextime.api.dto.CustomerSnapshotDto;
import com.dextime.api.entity.CustomerSnapshot;
import com.dextime.api.exception.BusinessException;
import com.dextime.api.repository.CustomerSnapshotRepository;
import com.dextime.api.service.CustomerSnapshotService;
import com.dextime.api.service.RabbitMQPublisherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerSnapshotService customerSnapshotService;
    private final RabbitMQPublisherService rabbitMQPublisherService;
    private final CustomerSnapshotRepository customerSnapshotRepository;

    public CustomerController(CustomerSnapshotService customerSnapshotService, 
                            RabbitMQPublisherService rabbitMQPublisherService,
                            CustomerSnapshotRepository customerSnapshotRepository) {
        this.customerSnapshotService = customerSnapshotService;
        this.rabbitMQPublisherService = rabbitMQPublisherService;
        this.customerSnapshotRepository = customerSnapshotRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerSnapshot createCustomer(@RequestBody CustomerSnapshotDto customerSnapshotDto) {
        CustomerSnapshot customer = customerSnapshotService.createCustomerSnapshot(customerSnapshotDto);
        rabbitMQPublisherService.publishMessage("customer", "create", customer.getId(), customerSnapshotDto);
        return customer;
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public List<CustomerSnapshot> getAllCustomersSnapshots() {
        return customerSnapshotService.getAllCustomerSnapshots();
    }

    @GetMapping("/profile")
    public CustomerSnapshot getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return customerSnapshotRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerSnapshot getCustomerSnapshotById(@PathVariable String id) {
        return customerSnapshotService.getCustomerSnapshotById(id);
    }

    @PutMapping("/profile")
    public CustomerSnapshot updateMyProfile(@RequestBody CustomerSnapshotDto customerSnapshotDto, Authentication authentication) {
        String email = authentication.getName();
        CustomerSnapshot customer = customerSnapshotRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found"));
        
        CustomerSnapshot updatedCustomer = customerSnapshotService.updateCustomerSnapshotById(customer.getId(), customerSnapshotDto);
        rabbitMQPublisherService.publishMessage("customer", "update", customer.getId(), customerSnapshotDto);
        return updatedCustomer;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerSnapshot updateCustomerSnapshotById(@PathVariable String id, @RequestBody CustomerSnapshotDto customerSnapshotDto) {
        CustomerSnapshot customer = customerSnapshotService.updateCustomerSnapshotById(id, customerSnapshotDto);
        rabbitMQPublisherService.publishMessage("customer", "update", id, customerSnapshotDto);
        return customer;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCustomerSnapshotById(@PathVariable String id) {
        customerSnapshotService.deleteCustomerSnapshotById(id);
        rabbitMQPublisherService.publishMessage("customer", "delete", id, null);
    }

}
