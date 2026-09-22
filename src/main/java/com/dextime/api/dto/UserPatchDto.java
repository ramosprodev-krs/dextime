package com.dextime.api.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UserPatchDto {
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
}
