package com.dextime.api.service;

import com.dextime.api.dto.ProviderSnapshotDto;
import com.dextime.api.entity.ProviderSnapshot;
import com.dextime.api.exception.ResourceNotFoundException;
import com.dextime.api.repository.ProviderSnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderSnapshotService {

    private static final String PROVIDER_SNAPSHOT_NOT_FOUND_MESSAGE = "ProviderSnapshot not found with id: ";

    private final ProviderSnapshotRepository providerSnapshotRepository;

    public ProviderSnapshotService(ProviderSnapshotRepository providerSnapshotRepository) {
        this.providerSnapshotRepository = providerSnapshotRepository;
    }

    // CRUD operations are listed below

    // Create provider snapshot with ProviderSnapshotDto
    public ProviderSnapshot createProviderSnapshot(ProviderSnapshotDto providerSnapshotDto) {
        ProviderSnapshot newProviderSnapshot = new ProviderSnapshot();
        newProviderSnapshot.setId(providerSnapshotDto.getId());
        newProviderSnapshot.setName(providerSnapshotDto.getName());
        newProviderSnapshot.setEmail(providerSnapshotDto.getEmail());
        newProviderSnapshot.setPhone(providerSnapshotDto.getPhone());
        newProviderSnapshot.setAddress(providerSnapshotDto.getAddress());

        return providerSnapshotRepository.save(newProviderSnapshot);
    }

    // Read all provider snapshots
    public List<ProviderSnapshot> getAllProviderSnapshots() {
        return providerSnapshotRepository.findAll();
    }

    // Read provider snapshot by id
    public ProviderSnapshot getProviderSnapshotById(String id) {
        return providerSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROVIDER_SNAPSHOT_NOT_FOUND_MESSAGE + id));
    }

    // Update provider snapshot by id with ProviderSnapshotDto
    public ProviderSnapshot updateProviderSnapshotById(String id, ProviderSnapshotDto providerSnapshotDto) {
        ProviderSnapshot selectedProviderSnapshot = providerSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROVIDER_SNAPSHOT_NOT_FOUND_MESSAGE + id));

        selectedProviderSnapshot.setName(providerSnapshotDto.getName());
        selectedProviderSnapshot.setEmail(providerSnapshotDto.getEmail());
        selectedProviderSnapshot.setPhone(providerSnapshotDto.getPhone());
        selectedProviderSnapshot.setAddress(providerSnapshotDto.getAddress());

        return providerSnapshotRepository.save(selectedProviderSnapshot);
    }

    public void deleteProviderSnapshotById(String id) {
        ProviderSnapshot selectedProviderSnapshot = providerSnapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROVIDER_SNAPSHOT_NOT_FOUND_MESSAGE + id));

        providerSnapshotRepository.delete(selectedProviderSnapshot);
    }
}
