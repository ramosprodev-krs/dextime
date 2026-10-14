package com.dextime.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureResponseDto {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private List<LocalDateTime> availableDates = new ArrayList<>();
    private String storeId;
    private String storeName;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}