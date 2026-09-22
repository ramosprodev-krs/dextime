package com.dextime.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Document(value = "appointments")
public class Appointment {

    /*
     * Unique identifier
    */
    @Id
    private String id;

    /*
     * Appointment data
    */
    private AppointmentStatus status;
    private LocalDateTime date;

    /*
     * References
     */
    @NotNull(message = "Customer is required")
    @DocumentReference(lazy = true)
    @JsonIgnoreProperties({"stores", "password"})
    private UserEntity customer;

    @DocumentReference(lazy = true)
    @JsonIgnoreProperties({"stores", "password"})
    private UserEntity provider;

    @DocumentReference(lazy = true)
    @JsonIgnoreProperties({"store"})
    private Procedure procedure;

    @DocumentReference(lazy = true)
    @JsonIgnoreProperties({"appointments", "procedures", "manager", "employees"})
    private Store store;

    /*
     * Audit data
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
