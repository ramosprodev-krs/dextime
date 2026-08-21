package com.dextime.api.controller;

import com.dextime.api.dto.AppointmentDto;
import com.dextime.api.entity.Appointment;
import com.dextime.api.service.AppointmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping()
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public Appointment createAppointment(@RequestBody AppointmentDto appointmentDto) {
        return appointmentService.createAppointment(appointmentDto);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER') or hasRole('ADMIN')")
    public Appointment getAppointmentById(@PathVariable String id) {
        return appointmentService.getAppointmentById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER') or hasRole('ADMIN')")
    public Appointment updateAppointmentById(@PathVariable String id, @RequestBody AppointmentDto appointmentDto) {
        return appointmentService.updateAppointmentById(id, appointmentDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public void deleteAppointmentById(@PathVariable String id) {
        appointmentService.deleteAppointmentById(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public Appointment approveAppointment(@PathVariable String id) {
        return appointmentService.approveAppointment(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public Appointment rejectAppointment(@PathVariable String id) {
        return appointmentService.rejectAppointment(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public Appointment cancelAppointment(@PathVariable String id) {
        return appointmentService.cancelAppointment(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('PROVIDER') or hasRole('ADMIN')")
    public Appointment completeAppointment(@PathVariable String id) {
        return appointmentService.completeAppointment(id);
    }
}
