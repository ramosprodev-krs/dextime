package com.dextime.api.repository;

import com.dextime.api.entity.Appointment;
import com.dextime.api.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByCustomerOrderByDateDesc(UserEntity customer);
}
