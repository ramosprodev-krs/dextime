package com.dextime.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CustomerSnapshotDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
