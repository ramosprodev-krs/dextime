package com.dextime.api.service;

import com.dextime.api.dto.AppointmentDto;
import com.dextime.api.dto.CustomerSnapshotDto;
import com.dextime.api.dto.ProviderSnapshotDto;
import com.dextime.api.dto.StoreSnapshotDto;
import com.dextime.api.entity.Appointment;
import com.dextime.api.entity.AppointmentStatus;
import com.dextime.api.entity.CustomerSnapshot;
import com.dextime.api.entity.ProviderSnapshot;
import com.dextime.api.entity.StoreSnapshot;
import com.dextime.api.exception.BusinessException;
import com.dextime.api.exception.ResourceNotFoundException;
import com.dextime.api.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AppointmentService {

    private static final String APPOINTMENT_NOT_FOUND_MESSAGE = "Appointment not found with id: ";

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    // CRUD operations are listed below

    // Create appointment with AppointmentDto
    public Appointment createAppointment(AppointmentDto appointmentDto) {
        Appointment newAppointment = new Appointment();
        newAppointment.setCustomer(toCustomerSnapshotEntity(appointmentDto.getCustomer()));
        newAppointment.setStore(toStoreSnapshotEntity(appointmentDto.getStore()));
        newAppointment.setProvider(toProviderSnapshotEntity(appointmentDto.getProvider()));
        newAppointment.setServiceId(appointmentDto.getServiceId());
        newAppointment.setServiceName(appointmentDto.getServiceName());
        newAppointment.setServicePrice(appointmentDto.getServicePrice());
        newAppointment.setAppointmentDate(appointmentDto.getAppointmentDate());
        newAppointment.setAppointmentStatus(appointmentDto.getAppointmentStatus() != null ? 
                appointmentDto.getAppointmentStatus() : AppointmentStatus.PENDING);
        newAppointment.setNotes(appointmentDto.getNotes());
        newAppointment.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        newAppointment.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));

        return appointmentRepository.save(newAppointment);
    }

    // Read all appointments
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // Read appointment by id
    public Appointment getAppointmentById(String id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE + id));
    }

    // Update appointment by id with AppointmentDto
    public Appointment updateAppointmentById(String id, AppointmentDto appointmentDto) {
        Appointment selectedAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE + id));

        selectedAppointment.setCustomer(toCustomerSnapshotEntity(appointmentDto.getCustomer()));
        selectedAppointment.setStore(toStoreSnapshotEntity(appointmentDto.getStore()));
        selectedAppointment.setProvider(toProviderSnapshotEntity(appointmentDto.getProvider()));
        selectedAppointment.setServiceId(appointmentDto.getServiceId());
        selectedAppointment.setServiceName(appointmentDto.getServiceName());
        selectedAppointment.setServicePrice(appointmentDto.getServicePrice());
        selectedAppointment.setAppointmentDate(appointmentDto.getAppointmentDate());
        selectedAppointment.setAppointmentStatus(appointmentDto.getAppointmentStatus());
        selectedAppointment.setNotes(appointmentDto.getNotes());
        selectedAppointment.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));

        return appointmentRepository.save(selectedAppointment);
    }

    public void deleteAppointmentById(String id) {
        Appointment selectedAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE + id));

        appointmentRepository.delete(selectedAppointment);
    }

    // Approve appointment (provider action)
    public Appointment approveAppointment(String id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE + id));

        if (appointment.getAppointmentStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException("Can only approve pending appointments");
        }

        appointment.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));

        return appointmentRepository.save(appointment);
    }

    // Reject appointment (provider action)
    public Appointment rejectAppointment(String id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE + id));

        if (appointment.getAppointmentStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException("Can only reject pending appointments");
        }

        appointment.setAppointmentStatus(AppointmentStatus.REJECTED);
        appointment.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));

        return appointmentRepository.save(appointment);
    }

    // Cancel appointment (customer action)
    public Appointment cancelAppointment(String id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE + id));

        if (appointment.getAppointmentStatus() == AppointmentStatus.COMPLETED || 
            appointment.getAppointmentStatus() == AppointmentStatus.REJECTED) {
            throw new BusinessException("Cannot cancel completed or rejected appointments");
        }

        appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));

        return appointmentRepository.save(appointment);
    }

    // Complete appointment (provider action)
    public Appointment completeAppointment(String id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(APPOINTMENT_NOT_FOUND_MESSAGE + id));

        if (appointment.getAppointmentStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Can only complete confirmed appointments");
        }

        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));

        return appointmentRepository.save(appointment);
    }

    private CustomerSnapshot toCustomerSnapshotEntity(CustomerSnapshotDto dto) {
        if (dto == null) return null;
        CustomerSnapshot snapshot = new CustomerSnapshot();
        snapshot.setId(dto.getId());
        snapshot.setFirstName(dto.getFirstName());
        snapshot.setLastName(dto.getLastName());
        snapshot.setEmail(dto.getEmail());
        snapshot.setPhone(dto.getPhone());
        return snapshot;
    }

    private StoreSnapshot toStoreSnapshotEntity(StoreSnapshotDto dto) {
        if (dto == null) return null;
        StoreSnapshot snapshot = new StoreSnapshot();
        snapshot.setId(dto.getId());
        snapshot.setName(dto.getName());
        snapshot.setAddress(dto.getAddress());
        return snapshot;
    }

    private ProviderSnapshot toProviderSnapshotEntity(ProviderSnapshotDto dto) {
        if (dto == null) return null;
        ProviderSnapshot snapshot = new ProviderSnapshot();
        snapshot.setId(dto.getId());
        snapshot.setName(dto.getName());
        snapshot.setEmail(dto.getEmail());
        snapshot.setPhone(dto.getPhone());
        snapshot.setAddress(dto.getAddress());
        return snapshot;
    }
}
