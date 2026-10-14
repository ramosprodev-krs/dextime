package com.dextime.api.controller;

import com.dextime.api.dto.AppointmentDto;
import com.dextime.api.dto.AppointmentResponseDto;
import com.dextime.api.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PostMapping("/request")
    public ResponseEntity<AppointmentResponseDto> requestAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AppointmentDto dto) {

        return ResponseEntity.ok(appointmentService.requestAppointment(userDetails.getUsername(), dto));
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponseDto>> myAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(appointmentService.readMyAppointments(userDetails.getUsername()));
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @PostMapping("/cancel/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String appointmentId) {

        return ResponseEntity.ok(appointmentService.cancelAppointment(userDetails.getUsername(), appointmentId));
    }

    @PreAuthorize("hasAuthority('PROVIDER')")
    @PostMapping("/assign/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> assignAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String appointmentId) {

        return ResponseEntity.ok(appointmentService.assignAppointment(userDetails.getUsername(), appointmentId));
    }

    @PreAuthorize("hasAuthority('PROVIDER')")
    @PostMapping("/complete/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> completeAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String appointmentId) {

        return ResponseEntity.ok(appointmentService.completeAppointment(userDetails.getUsername(), appointmentId));
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'PROVIDER')")
    @GetMapping("/{storeId}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> readAppointmentsByStoreId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String storeId) {

        return ResponseEntity.ok(appointmentService.readAppointmentsByStore(userDetails.getUsername(), storeId));
    }
}