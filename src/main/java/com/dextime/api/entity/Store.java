package com.dextime.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Document(value = "stores")
public class Store {

    /*
     * Unique identifier
     */
    @Id
    private String id;

    /*
     * Store data
     */
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "CNPJ is required")
    @CNPJ(message = "CNPJ has to be in the format: XXX.XXX.XXX/XXXX-XX")
    private String cnpj;

    /*
     * References
     */
    @DocumentReference(lazy = true)
    @JsonIgnoreProperties({"stores", "password"})
    private UserEntity manager;

    @DocumentReference(lazy = true)
    private List<UserEntity> providers = new ArrayList<>();

    @DocumentReference(lazy = true)
    private List<Appointment> appointments = new ArrayList<>();

    @DocumentReference(lazy = true)
    private List<Procedure> procedures = new ArrayList<>();

    /*
     * Audit data
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
