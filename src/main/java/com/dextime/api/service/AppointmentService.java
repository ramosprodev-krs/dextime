package com.dextime.api.service;

import com.dextime.api.dto.AppointmentDto;
import com.dextime.api.entity.*;
import com.dextime.api.exception.CustomerNotFoundException;
import com.dextime.api.exception.ProcedureNotFoundException;
import com.dextime.api.exception.StoreNotFoundException;
import com.dextime.api.exception.UnavailableDateException;
import com.dextime.api.repository.AppointmentRepository;
import com.dextime.api.repository.ProcedureRepository;
import com.dextime.api.repository.StoreRepository;
import com.dextime.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AppointmentService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProcedureRepository procedureRepository;
    private final StoreRepository storeRepository;


    public AppointmentService(
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            ProcedureRepository procedureRepository,
            StoreRepository storeRepository) {

        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.procedureRepository = procedureRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public Appointment requestAppointment(String username, AppointmentDto appointmentDto) {
        LocalDateTime now = LocalDateTime.now();

        UserEntity customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Store store = storeRepository.findById(appointmentDto.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        Procedure procedure = procedureRepository.findByIdAndStore(appointmentDto.getProcedureId(), store)
                .orElseThrow(() -> new ProcedureNotFoundException("Procedure not found in store"));

        if(!procedure.getAvailableDates().contains(appointmentDto.getDate())){
            throw new UnavailableDateException("Date is not available");
        }

        Appointment appointment = new Appointment();

        appointment.setCustomer(customer);
        appointment.setStore(store);
        appointment.setProcedure(procedure);
        appointment.setDate(appointmentDto.getDate());
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        appointment.setStatus(AppointmentStatus.PENDING);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        procedure.getAvailableDates().remove(appointmentDto.getDate());
        procedureRepository.save(procedure);

        store.getAppointments().add(savedAppointment);
        storeRepository.save(store);

        return savedAppointment;
    }
}
