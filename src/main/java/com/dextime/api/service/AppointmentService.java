package com.dextime.api.service;

import com.dextime.api.dto.AppointmentDto;
import com.dextime.api.dto.AppointmentResponseDto;
import com.dextime.api.entity.*;
import com.dextime.api.exception.*;
import com.dextime.api.repository.AppointmentRepository;
import com.dextime.api.repository.ProcedureRepository;
import com.dextime.api.repository.StoreRepository;
import com.dextime.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AppointmentService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProcedureRepository procedureRepository;
    private final StoreRepository storeRepository;
    private final EmailProducerService emailProducerService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AppointmentService(
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            ProcedureRepository procedureRepository,
            StoreRepository storeRepository,
            EmailProducerService emailProducerService) {

        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.procedureRepository = procedureRepository;
        this.storeRepository = storeRepository;
        this.emailProducerService = emailProducerService;
    }

    @Transactional
    public AppointmentResponseDto requestAppointment(String username, AppointmentDto appointmentDto) {
        LocalDateTime now = LocalDateTime.now();

        UserEntity customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        Store store = storeRepository.findById(appointmentDto.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        Procedure procedure = procedureRepository.findByIdAndStore(appointmentDto.getProcedureId(), store)
                .orElseThrow(() -> new ProcedureNotFoundException("Procedure not found in store"));

        if (!procedure.getAvailableDates().contains(appointmentDto.getDate())) {
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

        emailProducerService.sendEmailToQueue(
                customer.getEmail(),
                "Appointment Requested Successfully",
                "emails/appointment-requested",
                Map.of(
                        "customerName", customer.getUsername(),
                        "storeName", store.getName(),
                        "procedureName", procedure.getName(),
                        "appointmentDate", formatDate(appointmentDto.getDate())
                )
        );

        return mapToResponseDto(savedAppointment);
    }

    /** Provider assume o agendamento: so vale para PENDING, sem provider, de uma loja onde ele trabalha. */
    @Transactional
    public AppointmentResponseDto assignAppointment(String username, String appointmentId) {
        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found."));

        UserEntity provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

        boolean worksAtStore = provider.getProvidedStores().stream()
                .anyMatch(store -> store.getId().equals(appointment.getStore().getId()));

        if (!worksAtStore) {
            throw new UnauthorizedException("Provider is not authorized to assign this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING || hasProvider(appointment)) {
            throw new InvalidAppointmentStateException("Appointment is no longer available");
        }

        appointment.setProvider(provider);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(now);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        provider.getProvidedAppointments().add(appointment);
        provider.setUpdatedAt(now);
        userRepository.save(provider);

        emailProducerService.sendEmailToQueue(
                appointment.getCustomer().getEmail(),
                "Appointment Confirmed!",
                "emails/appointment-confirmed",
                Map.of(
                        "customerName", appointment.getCustomer().getUsername(),
                        "storeName", appointment.getStore().getName(),
                        "procedureName", appointment.getProcedure().getName(),
                        "providerName", provider.getUsername(),
                        "appointmentDate", formatDate(savedAppointment.getDate())
                )
        );

        emailProducerService.sendEmailToQueue(
                provider.getEmail(),
                "New Appointment Assigned to You",
                "emails/appointment-assigned-provider",
                Map.of(
                        "providerName", provider.getUsername(),
                        "customerName", appointment.getCustomer().getUsername(),
                        "storeName", appointment.getStore().getName(),
                        "procedureName", appointment.getProcedure().getName(),
                        "appointmentDate", formatDate(savedAppointment.getDate())
                )
        );

        return mapToResponseDto(savedAppointment);
    }

    /** Provider responsavel conclui o atendimento (CONFIRMED -> COMPLETED). */
    @Transactional
    public AppointmentResponseDto completeAppointment(String username, String appointmentId) {
        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

        UserEntity provider = userRepository.findByUsername(username)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found"));

        if (!hasProvider(appointment) || !appointment.getProvider().getId().equals(provider.getId())) {
            throw new UnauthorizedException("User is not the provider of this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStateException("Appointment is not confirmed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(now);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        UserEntity customer = savedAppointment.getCustomer();

        emailProducerService.sendEmailToQueue(
                customer.getEmail(),
                "Appointment Completed - Thank You!",
                "emails/appointment-completed",
                Map.of(
                        "customerName", customer.getUsername(),
                        "storeName", savedAppointment.getStore().getName(),
                        "procedureName", savedAppointment.getProcedure().getName(),
                        "appointmentDate", formatDate(savedAppointment.getDate())
                )
        );

        return mapToResponseDto(savedAppointment);
    }

    /** Cliente cancela o proprio agendamento (PENDING ou CONFIRMED) e a data volta a ficar disponivel. */
    @Transactional
    public AppointmentResponseDto cancelAppointment(String username, String appointmentId) {
        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

        UserEntity customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        if (!appointment.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("User is not the customer of this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentStateException("Only pending or confirmed appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(now);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        Procedure procedure = savedAppointment.getProcedure();
        if (!procedure.getAvailableDates().contains(savedAppointment.getDate())) {
            procedure.getAvailableDates().add(savedAppointment.getDate());
            procedureRepository.save(procedure);
        }

        emailProducerService.sendEmailToQueue(
                customer.getEmail(),
                "Appointment Cancelled",
                "emails/appointment-cancelled",
                Map.of(
                        "recipientName", customer.getUsername(),
                        "customerName", customer.getUsername(),
                        "storeName", savedAppointment.getStore().getName(),
                        "procedureName", savedAppointment.getProcedure().getName(),
                        "appointmentDate", formatDate(savedAppointment.getDate())
                )
        );

        UserEntity provider = savedAppointment.getProvider();
        if (hasProvider(savedAppointment)) {
            emailProducerService.sendEmailToQueue(
                    provider.getEmail(),
                    "Appointment Cancelled",
                    "emails/appointment-cancelled",
                    Map.of(
                            "recipientName", provider.getUsername(),
                            "customerName", customer.getUsername(),
                            "storeName", savedAppointment.getStore().getName(),
                            "procedureName", savedAppointment.getProcedure().getName(),
                            "appointmentDate", formatDate(savedAppointment.getDate())
                    )
            );
        }

        return mapToResponseDto(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> readMyAppointments(String username) {
        UserEntity customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        return appointmentRepository.findByCustomerOrderByDateDesc(customer)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> readAppointmentsByStore(String username, String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean isManager = store.getManager() != null && store.getManager().getId().equals(user.getId());
        boolean isProvider = user.getProvidedStores().stream()
                .anyMatch(providedStore -> providedStore.getId() != null && providedStore.getId().equals(store.getId()));

        if (!isManager && !isProvider) {
            throw new UnauthorizedException("User is not authorized to view appointments for this store");
        }

        return store.getAppointments()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
    }

    /** Um provider "de verdade" tem id; um objeto vazio/placeholder nao conta como responsavel. */
    private boolean hasProvider(Appointment appointment) {
        return appointment.getProvider() != null && appointment.getProvider().getId() != null;
    }

    private AppointmentResponseDto mapToResponseDto(Appointment appointment) {
        String providerUsername = hasProvider(appointment)
                ? appointment.getProvider().getUsername()
                : null;

        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getStatus(),
                appointment.getDate(),
                appointment.getCustomer().getUsername(),
                providerUsername,
                appointment.getStore().getName(),
                appointment.getProcedure().getName(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}