package com.dextime.api.controller;

import com.dextime.api.dto.AppointmentDto;
import com.dextime.api.entity.Appointment;
import com.dextime.api.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Appointment> requestAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AppointmentDto dto) {

        return ResponseEntity.ok(appointmentService.requestAppointment(userDetails.getUsername(), dto));
    }
}
