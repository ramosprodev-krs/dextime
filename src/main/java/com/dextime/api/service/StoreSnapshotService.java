package com.dextime.api.service;

import com.dextime.api.dto.StoreSnapshotDto;
import com.dextime.api.entity.StoreSnapshot;
import com.dextime.api.exception.ResourceNotFoundException;
import com.dextime.api.repository.StoreSnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreSnapshotService {

    private static final String STORE_SNAPSHOT_NOT_FOUND_MESSAGE = "StoreSnapshot not found with id: ";

    private final StoreSnapshotRepository storeSnapshotRepository;

    public StoreSnapshotService(StoreSnapshotRepository storeSnapshotRepository) {
        this.storeSnapshotRepository = storeSnapshotRepository;
    }

    // CRUD operations are listed below

    // Create store snapshot with StoreSnapshotDto and providerId
    public StoreSnapshot createStoreSnapshot(StoreSnapshotDto storeSnapshotDto, String providerId) {
        StoreSnapshot newStoreSnapshot = new StoreSnapshot();
        newStoreSnapshot.setId(storeSnapshotDto.getId());
        newStoreSnapshot.setProviderId(providerId);
        newStoreSnapshot.setName(storeSnapshotDto.getName());
        newStoreSnapshot.setAddress(storeSnapshotDto.getAddress());

        return storeSnapshotRepository.save(newStoreSnapshot);
    }

    // Read all store snapshots
    public List<StoreSnapshot> getAllStoreSnapshots() {
        return storeSnapshotRepository.findAll();
    }

    // Read store snapshots by provider ID
    public List<StoreSnapshot> getStoreSnapshotsByProviderId(String providerId) {
        return storeSnapshotRepository.findByProviderId(providerId);
    }

    // Read store snapshot by id
    public StoreSnapshot getStoreSnapshotById(String id) {
        return storeSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STORE_SNAPSHOT_NOT_FOUND_MESSAGE + id));
    }

    // Update store snapshot by id with StoreSnapshotDto
    public StoreSnapshot updateStoreSnapshotById(String id, StoreSnapshotDto storeSnapshotDto) {
        StoreSnapshot selectedStoreSnapshot = storeSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STORE_SNAPSHOT_NOT_FOUND_MESSAGE + id));

        selectedStoreSnapshot.setName(storeSnapshotDto.getName());
        selectedStoreSnapshot.setAddress(storeSnapshotDto.getAddress());

        return storeSnapshotRepository.save(selectedStoreSnapshot);
    }

    public void deleteStoreSnapshotById(String id) {
        StoreSnapshot selectedStoreSnapshot = storeSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STORE_SNAPSHOT_NOT_FOUND_MESSAGE + id));

        storeSnapshotRepository.delete(selectedStoreSnapshot);
    }
}
