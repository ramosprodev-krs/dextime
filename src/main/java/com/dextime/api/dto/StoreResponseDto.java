package com.dextime.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponseDto {
    private String id;
    private String name;
    private String cnpj;
    private String managerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}