package com.dextime.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "appointments")
public class Appointment {
    @Id
    private String id;

    // --- Relationships and Snapshots ---
    @Indexed
    private CustomerSnapshot customer;

    @Indexed
    private StoreSnapshot store;

    @Indexed
    private ProviderSnapshot provider;

    // --- Service Details ---
    private String serviceId;
    private String serviceName;
    private BigDecimal servicePrice;

    // --- Appointment and Status ---
    @Indexed
    @Field("appointment_date")
    private LocalDateTime appointmentDate;

    @Field("status")
    private AppointmentStatus appointmentStatus;

    private String notes;

    // --- Auditor Fields ---
    @Indexed
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}
