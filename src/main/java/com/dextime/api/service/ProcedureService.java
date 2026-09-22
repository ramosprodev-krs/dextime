package com.dextime.api.service;

import com.dextime.api.dto.ProcedureDto;
import com.dextime.api.entity.Procedure;
import com.dextime.api.entity.Store;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.exception.ManagerNotFoundException;
import com.dextime.api.exception.StoreNotFoundException;
import com.dextime.api.exception.UnauthorizedException;
import com.dextime.api.repository.ProcedureRepository;
import com.dextime.api.repository.StoreRepository;
import com.dextime.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcedureService {

    private final ProcedureRepository procedureRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public ProcedureService(ProcedureRepository procedureRepository, UserRepository userRepository,
                            StoreRepository storeRepository) {
        this.procedureRepository = procedureRepository;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
    }


    @Transactional
    public Procedure createProcedure(String username, ProcedureDto procedureDto) {
        LocalDateTime now = LocalDateTime.now();

        UserEntity manager = userRepository.findByUsername(username)
                .orElseThrow(() -> new ManagerNotFoundException("Manager not found"));

        Store store = storeRepository.findById(procedureDto.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        if (!store.getManager().getId().equals(manager.getId())) {
            throw new UnauthorizedException("User is not the manager of this store");
        }

        Procedure procedure = new Procedure();
        procedure.setName(procedureDto.getName());
        procedure.setPrice(procedureDto.getPrice());
        procedure.setDescription(procedureDto.getDescription());
        procedure.setAvailableDates(procedureDto.getAvailableDates());
        procedure.setStore(store);
        procedure.setCreatedBy(manager);
        procedure.setCreatedAt(now);
        procedure.setUpdatedAt(now);

        Procedure savedProcedure = procedureRepository.save(procedure);

        store.getProcedures().add(savedProcedure);
        storeRepository.save(store);

        return savedProcedure;
    }

}
