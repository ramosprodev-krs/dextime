package com.dextime.api.service;

import com.dextime.api.dto.CustomerSnapshotDto;
import com.dextime.api.entity.CustomerSnapshot;
import com.dextime.api.exception.ResourceNotFoundException;
import com.dextime.api.repository.CustomerSnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerSnapshotService {

    private static final String CUSTOMER_SNAPSHOT_NOT_FOUND_MESSAGE = "CustomerSnapshot not found with id: ";

    private final CustomerSnapshotRepository customerSnapshotRepository;

    public CustomerSnapshotService(CustomerSnapshotRepository customerSnapshotRepository) {
        this.customerSnapshotRepository = customerSnapshotRepository;
    }

    // CRUD operations are listed below

    // Create customer snapshot with CustomerSnapshotDto
    public CustomerSnapshot createCustomerSnapshot(CustomerSnapshotDto customerSnapshotDto) {
        CustomerSnapshot newCustomerSnapshot = new CustomerSnapshot();
        newCustomerSnapshot.setId(customerSnapshotDto.getId());
        newCustomerSnapshot.setFirstName(customerSnapshotDto.getFirstName());
        newCustomerSnapshot.setLastName(customerSnapshotDto.getLastName());
        newCustomerSnapshot.setEmail(customerSnapshotDto.getEmail());
        newCustomerSnapshot.setPhone(customerSnapshotDto.getPhone());

        return customerSnapshotRepository.save(newCustomerSnapshot);
    }

    // Read all customer snapshots
    public List<CustomerSnapshot> getAllCustomerSnapshots() {
        return customerSnapshotRepository.findAll();
    }

    // Read customer snapshot by id
    public CustomerSnapshot getCustomerSnapshotById(String id) {
        return customerSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_SNAPSHOT_NOT_FOUND_MESSAGE + id));
    }

    // Update customer snapshot by id with CustomerSnapshotDto
    public CustomerSnapshot updateCustomerSnapshotById(String id, CustomerSnapshotDto customerSnapshotDto) {
        CustomerSnapshot selectedCustomerSnapshot = customerSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_SNAPSHOT_NOT_FOUND_MESSAGE + id));

        selectedCustomerSnapshot.setFirstName(customerSnapshotDto.getFirstName());
        selectedCustomerSnapshot.setLastName(customerSnapshotDto.getLastName());
        selectedCustomerSnapshot.setEmail(customerSnapshotDto.getEmail());
        selectedCustomerSnapshot.setPhone(customerSnapshotDto.getPhone());

        return customerSnapshotRepository.save(selectedCustomerSnapshot);
    }

    public void deleteCustomerSnapshotById(String id) {
        CustomerSnapshot selectedCustomerSnapshot = customerSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CUSTOMER_SNAPSHOT_NOT_FOUND_MESSAGE + id));

        customerSnapshotRepository.delete(selectedCustomerSnapshot);
    }
}
