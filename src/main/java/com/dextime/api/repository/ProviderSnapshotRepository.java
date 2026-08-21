package com.dextime.api.repository;

import com.dextime.api.entity.ProviderSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderSnapshotRepository extends MongoRepository<ProviderSnapshot, String> {
    Optional<ProviderSnapshot> findByUserId(String userId);
}
