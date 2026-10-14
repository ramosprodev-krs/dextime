package com.dextime.api.dto;

import com.dextime.api.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDto {
    private String id;
    private AppointmentStatus status;
    private LocalDateTime date;
    private String customerUsername;
    private String providerUsername;
    private String storeName;
    private String procedureName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}