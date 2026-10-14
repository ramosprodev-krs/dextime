package com.dextime.api.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor


@Document(value = "procedures")
public class Procedure {

    /*
     * Unique identifier
     */
    @Id
    private String id;

    /*
     * Procedure data
     */
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "At least one available date is required")
    private List<LocalDateTime> availableDates = new ArrayList<>();

    /*
     * References
     */
    @DocumentReference(lazy = true)
    private Store store;

    @DocumentReference
    private UserEntity createdBy;

    /*
     * Audit data
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
