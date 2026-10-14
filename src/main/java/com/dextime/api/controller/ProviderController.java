package com.dextime.api.controller;

import com.dextime.api.dto.ProviderSummaryDto;
import com.dextime.api.dto.StoreResponseDto;
import com.dextime.api.dto.UserDto;
import com.dextime.api.dto.UserResponseDto;
import com.dextime.api.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping
    public ResponseEntity<UserResponseDto> createProvider(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String storeId,
            @RequestBody @Valid UserDto dto) {

        String username = userDetails.getUsername();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(providerService.createProvider(username, storeId, dto));
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProviderSummaryDto>> getProvidersByStore(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String storeId) {

        return ResponseEntity.ok(providerService.getProvidersByStore(userDetails.getUsername(), storeId));
    }

    @PreAuthorize("hasAuthority('PROVIDER')")
    @GetMapping("/me/stores")
    public ResponseEntity<List<StoreResponseDto>> myStores(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(providerService.getProvidedStores(userDetails.getUsername()));
    }
}