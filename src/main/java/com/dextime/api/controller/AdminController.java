package com.dextime.api.controller;

import com.dextime.api.entity.Appointment;
import com.dextime.api.entity.CustomerSnapshot;
import com.dextime.api.entity.ProviderSnapshot;
import com.dextime.api.entity.StoreSnapshot;
import com.dextime.api.exception.ResourceNotFoundException;
import com.dextime.api.repository.AppointmentRepository;
import com.dextime.api.repository.CustomerSnapshotRepository;
import com.dextime.api.repository.ProviderSnapshotRepository;
import com.dextime.api.repository.StoreSnapshotRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CustomerSnapshotRepository customerSnapshotRepository;
    private final ProviderSnapshotRepository providerSnapshotRepository;
    private final StoreSnapshotRepository storeSnapshotRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminController(CustomerSnapshotRepository customerSnapshotRepository,
                          ProviderSnapshotRepository providerSnapshotRepository,
                          StoreSnapshotRepository storeSnapshotRepository,
                          AppointmentRepository appointmentRepository) {
        this.customerSnapshotRepository = customerSnapshotRepository;
        this.providerSnapshotRepository = providerSnapshotRepository;
        this.storeSnapshotRepository = storeSnapshotRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/users")
    public List<CustomerSnapshot> getAllUsers() {
        return customerSnapshotRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public CustomerSnapshot getUserById(@PathVariable String id) {
        return customerSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @GetMapping("/providers")
    public List<ProviderSnapshot> getAllProviders() {
        return providerSnapshotRepository.findAll();
    }

    @GetMapping("/providers/{id}")
    public ProviderSnapshot getProviderById(@PathVariable String id) {
        return providerSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", id));
    }

    @GetMapping("/stores")
    public List<StoreSnapshot> getAllStores() {
        return storeSnapshotRepository.findAll();
    }

    @GetMapping("/stores/{id}")
    public StoreSnapshot getStoreById(@PathVariable String id) {
        return storeSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
    }

    @GetMapping("/appointments")
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @GetMapping("/appointments/{id}")
    public Appointment getAppointmentById(@PathVariable String id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
    }
}
