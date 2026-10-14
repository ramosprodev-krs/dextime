package com.dextime.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email has to be in the format: example@example.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+[0-9]{2} [0-9]{11}$",
            message = "Phone number must be in the format: +XX XXXXXXXXXXX"
    )
    private String phoneNumber;

    @NotBlank(message = "CPF is required")
    @CPF(message = "CPF has to be in the format: XXX.XXX.XXX-XX")
    private String cpf;
}