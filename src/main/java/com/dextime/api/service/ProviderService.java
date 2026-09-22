package com.dextime.api.service;

import com.dextime.api.dto.UserDto;
import com.dextime.api.entity.Store;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.entity.UserRole;
import com.dextime.api.exception.*;
import com.dextime.api.repository.StoreRepository;
import com.dextime.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProviderService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public ProviderService(UserRepository userRepository, StoreRepository storeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserEntity createProvider(String username, String storeId, UserDto userDto) {
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(userRepository.existsByUsername(userDto.getUsername()))) {
            throw new UsernameConflictException("Username already exists");
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(userDto.getEmail()))) {
            throw new EmailConflictException("Email already exists");
        }

        if (Boolean.TRUE.equals(userRepository.existsByCpf(userDto.getCpf()))) {
            throw new CpfConflictException("CPF already exists");
        }

        UserEntity manager = userRepository.findByUsername(username)
                .orElseThrow(() -> new ManagerNotFoundException("Manager not found."));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("Store not found."));

        if (!store.getManager().getId().equals(manager.getId())) {
            throw new UnauthorizedException("User is not the manager of this store");
        }

        UserEntity provider = new UserEntity();
        provider.setUsername(userDto.getUsername());
        provider.setPassword(passwordEncoder.encode(userDto.getPassword()));
        provider.setFirstName(userDto.getFirstName());
        provider.setLastName(userDto.getLastName());
        provider.setEmail(userDto.getEmail());
        provider.setPhoneNumber(userDto.getPhoneNumber());
        provider.setCpf(userDto.getCpf());
        provider.setCreatedAt(now);
        provider.setUpdatedAt(now);
        provider.setRole(UserRole.PROVIDER);
        provider.getProvidedStores().add(store);

        UserEntity savedProvider = userRepository.save(provider);

        store.getProviders().add(savedProvider);
        storeRepository.save(store);

        return savedProvider;
    }
}
