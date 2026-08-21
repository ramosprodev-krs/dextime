package com.dextime.api.repository;

import com.dextime.api.entity.StoreSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreSnapshotRepository extends MongoRepository<StoreSnapshot, String> {
    List<StoreSnapshot> findByProviderId(String providerId);
}
