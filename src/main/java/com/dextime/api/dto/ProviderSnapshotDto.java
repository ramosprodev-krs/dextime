package com.dextime.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ProviderSnapshotDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
}
