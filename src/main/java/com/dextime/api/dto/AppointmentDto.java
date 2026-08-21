package com.dextime.api.dto;

import com.dextime.api.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class AppointmentDto {
    private String id;
    private CustomerSnapshotDto customer;
    private StoreSnapshotDto store;
    private ProviderSnapshotDto provider;
    private String serviceId;
    private String serviceName;
    private BigDecimal servicePrice;
    private LocalDateTime appointmentDate;
    private AppointmentStatus appointmentStatus;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
