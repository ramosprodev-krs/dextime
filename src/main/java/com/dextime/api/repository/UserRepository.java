package com.dextime.api.repository;

import com.dextime.api.entity.Store;
import com.dextime.api.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByCpf(String cpf);
    List<UserEntity> findByProvidedStoresContaining(Store store);
}
