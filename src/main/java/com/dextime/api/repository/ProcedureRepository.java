package com.dextime.api.repository;

import com.dextime.api.entity.Procedure;
import com.dextime.api.entity.Store;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProcedureRepository extends MongoRepository<Procedure, String> {
    Optional<Procedure> findByName(String name);
    Optional<Procedure> findByIdAndStore(String id, Store store);
}
