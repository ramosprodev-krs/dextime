package com.dextime.api.repository;

import com.dextime.api.entity.CustomerSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerSnapshotRepository extends MongoRepository<CustomerSnapshot, String> {
    Optional<CustomerSnapshot> findByEmail(String email);
}
