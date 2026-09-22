package com.dextime.api.controller;

import com.dextime.api.dto.StoreDto;
import com.dextime.api.entity.Appointment;
import com.dextime.api.entity.Store;
import com.dextime.api.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping
    public ResponseEntity<Store> createStore(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StoreDto storeDto) {

        String username = userDetails.getUsername();
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(username, storeDto));
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/{storeId}/appointments")
    public ResponseEntity<List<Appointment>> readAppointmentsByStoreId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String storeId) {

        return ResponseEntity.ok(storeService.readAppointmentsByStore(userDetails.getUsername(), storeId));
    }

    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('CUSTOMER')")
    @GetMapping("/{storeId}")
    public ResponseEntity<Store> readStoreById(@PathVariable String storeId) {
        return ResponseEntity.ok(storeService.readStoreById(storeId));
    }

    @PreAuthorize("hasAuthority('MANAGER') or hasAuthority('CUSTOMER')")
    @GetMapping
    public ResponseEntity<List<Store>> readAllStores() {
        return ResponseEntity.ok(storeService.readAllStores());
    }
}
