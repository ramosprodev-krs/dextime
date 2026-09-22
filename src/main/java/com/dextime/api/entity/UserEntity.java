package com.dextime.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "users")
public class UserEntity implements UserDetails {

    @Id
    private String id;

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

    @NotNull(message = "Role is required")
    private UserRole role;

    /*
     * References
     */
    @DocumentReference(lazy = true)
    @JsonIgnoreProperties({"manager", "providers"})
    private List<Store> managedStores = new ArrayList<>();

    @DocumentReference(lazy = true)
    @JsonIgnoreProperties({"providers", "manager"})
    private List<Store> providedStores = new ArrayList<>();

    @DocumentReference(lazy = true)
    private List<Appointment> appointments = new ArrayList<>();

    /*
     * Audit data
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /*
     * Security
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }
}
