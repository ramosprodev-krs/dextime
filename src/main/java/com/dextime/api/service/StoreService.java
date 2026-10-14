package com.dextime.api.service;

import com.dextime.api.dto.StoreDto;
import com.dextime.api.dto.StoreResponseDto;
import com.dextime.api.entity.Store;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.exception.ManagerNotFoundException;
import com.dextime.api.exception.StoreNotFoundException;
import com.dextime.api.repository.StoreRepository;
import com.dextime.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public StoreService(StoreRepository storeRepository, UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StoreResponseDto createStore(String username, StoreDto storeDto) {

        UserEntity manager = userRepository.findByUsername(username)
                .orElseThrow(() -> new ManagerNotFoundException("Manager not found"));

        LocalDateTime now = LocalDateTime.now();

        Store store = new Store();
        store.setName(storeDto.getName());
        store.setCnpj(storeDto.getCnpj());
        store.setManager(manager);
        store.setCreatedAt(now);
        store.setUpdatedAt(now);

        manager.getManagedStores().add(store);
        userRepository.save(manager);

        Store savedStore = storeRepository.save(store);

        return new StoreResponseDto(
                savedStore.getId(),
                savedStore.getName(),
                savedStore.getCnpj(),
                savedStore.getManager().getUsername(),
                savedStore.getCreatedAt(),
                savedStore.getUpdatedAt()
        );
    }

    public StoreResponseDto readStoreById(String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        return new StoreResponseDto(
                store.getId(),
                store.getName(),
                store.getCnpj(),
                store.getManager().getUsername(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }

    public List<StoreResponseDto> readAllStores() {
        List<Store> storesList = storeRepository.findAll();

        return storesList.stream()
                .map(store -> new StoreResponseDto(
                        store.getId(),
                        store.getName(),
                        store.getCnpj(),
                        store.getManager().getUsername(),
                        store.getCreatedAt(),
                        store.getUpdatedAt()
                ))
                .toList();
    }
}
