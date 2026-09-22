package com.dextime.api.service;

import com.dextime.api.dto.StoreDto;
import com.dextime.api.entity.Appointment;
import com.dextime.api.entity.Store;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.exception.ManagerNotFoundException;
import com.dextime.api.exception.StoreNotFoundException;
import com.dextime.api.exception.UnauthorizedException;
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
    public Store createStore(String username, StoreDto storeDto) {

        UserEntity manager = userRepository.findByUsername(username)
                .orElseThrow(() -> new ManagerNotFoundException("Manager not found"));

        LocalDateTime now = LocalDateTime.now();

        Store store = new Store();
        store.setName(storeDto.getName());
        store.setCnpj(storeDto.getCpnj());
        store.setManager(manager);
        store.setCreatedAt(now);
        store.setUpdatedAt(now);

        manager.getManagedStores().add(store);
        userRepository.save(manager);

        return storeRepository.save(store);
    }

    public List<Appointment> readAppointmentsByStore(String managerUsername, String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        UserEntity manager = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new ManagerNotFoundException("Manager not found"));

        if (!store.getManager().getId().equals(manager.getId())) {
            throw new UnauthorizedException("User is not the manager of this store");
        }

        return store.getAppointments();
    }

    public Store readStoreById(String storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));
    }

    public List<Store> readAllStores() {
        return storeRepository.findAll();
    }


}
