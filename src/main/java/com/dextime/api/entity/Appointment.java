package com.dextime.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Document(value = "appointments")
public class Appointment {

    @MongoId
    private String id;

    private String details;
    private Customer customer;
    private Provider provider;
    private Service service;
    private LocalDateTime date;

}
