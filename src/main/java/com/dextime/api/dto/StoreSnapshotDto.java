package com.dextime.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class StoreSnapshotDto {
    private String id;
    private String providerId;
    private String name;
    private String address;
}
