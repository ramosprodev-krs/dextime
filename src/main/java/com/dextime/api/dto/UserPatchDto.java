package com.dextime.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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

    @Email(message = "Email has to be in the format: example@example.com")
    private String email;

    @Pattern(
            regexp = "^\\+[0-9]{2} [0-9]{11}$",
            message = "Phone number must be in the format: +XX XXXXXXXXXXX"
    )
    private String phoneNumber;
}