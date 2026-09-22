package com.dextime.api.repository;

import com.dextime.api.entity.Store;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StoreRepository extends MongoRepository<Store, String> {
    Optional<Store> findByName(String name);
}
