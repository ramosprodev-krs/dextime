package com.dextime.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {

    @NotBlank(message = "Store ID is required")
    private String storeId;

    @NotBlank(message = "Procedure ID is required")
    private String procedureId;

    @NotNull(message = "Date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime date;
}