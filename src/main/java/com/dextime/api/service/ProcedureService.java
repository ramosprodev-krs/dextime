package com.dextime.api.service;

import com.dextime.api.dto.AddDatesDto;
import com.dextime.api.dto.ProcedureDto;
import com.dextime.api.dto.ProcedureResponseDto;
import com.dextime.api.entity.Procedure;
import com.dextime.api.entity.Store;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.exception.ManagerNotFoundException;
import com.dextime.api.exception.ProcedureNotFoundException;
import com.dextime.api.exception.StoreNotFoundException;
import com.dextime.api.exception.UnauthorizedException;
import com.dextime.api.repository.ProcedureRepository;
import com.dextime.api.repository.StoreRepository;
import com.dextime.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public ProcedureResponseDto createProcedure(String username, ProcedureDto procedureDto) {
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

        return new ProcedureResponseDto(
                savedProcedure.getId(),
                savedProcedure.getName(),
                savedProcedure.getDescription(),
                savedProcedure.getPrice(),
                savedProcedure.getAvailableDates(),
                savedProcedure.getStore().getId(),
                savedProcedure.getStore().getName(),
                savedProcedure.getCreatedBy().getUsername(),
                savedProcedure.getCreatedAt(),
                savedProcedure.getUpdatedAt()
        );
    }

    @Transactional
    public List<ProcedureResponseDto> getProceduresByStore(String id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new StoreNotFoundException("Store not found"));

        List<Procedure> procedureList = store.getProcedures();

        return procedureList.stream()
                .map(procedure -> new ProcedureResponseDto(
                        procedure.getId(),
                        procedure.getName(),
                        procedure.getDescription(),
                        procedure.getPrice(),
                        procedure.getAvailableDates(),
                        procedure.getStore().getId(),
                        procedure.getStore().getName(),
                        procedure.getCreatedBy().getUsername(),
                        procedure.getCreatedAt(),
                        procedure.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional
    public ProcedureResponseDto addDatesToProcedure(String username, String procedureId, AddDatesDto dto) {
        LocalDateTime now = LocalDateTime.now();

        UserEntity manager = userRepository.findByUsername(username)
                .orElseThrow(() -> new ManagerNotFoundException("Manager not found"));

        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new ProcedureNotFoundException("Procedure not found"));

        Store store = procedure.getStore();

        if (store.getManager() == null || !store.getManager().getId().equals(manager.getId())) {
            throw new UnauthorizedException("User is not the manager of this store");
        }

        dto.getDates().stream()
                .filter(date -> !procedure.getAvailableDates().contains(date))
                .forEach(procedure.getAvailableDates()::add);

        procedure.setUpdatedAt(now);
        Procedure saved = procedureRepository.save(procedure);

        return new ProcedureResponseDto(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getAvailableDates(),
                saved.getStore().getId(),
                saved.getStore().getName(),
                saved.getCreatedBy().getUsername(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

}