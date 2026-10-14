package com.dextime.api.service;

import com.dextime.api.dto.UserDto;
import com.dextime.api.dto.UserResponseDto;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.entity.UserRole;
import com.dextime.api.exception.*;
import com.dextime.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ManagerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDto createManager(UserDto userDto) {
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

        UserEntity user = new UserEntity();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setCpf(userDto.getCpf());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setRole(UserRole.MANAGER);

        UserEntity savedManager = userRepository.save(user);

        return new UserResponseDto(
                savedManager.getId(),
                savedManager.getUsername(),
                savedManager.getFirstName(),
                savedManager.getLastName(),
                savedManager.getEmail(),
                savedManager.getPhoneNumber(),
                savedManager.getCpf(),
                savedManager.getRole(),
                savedManager.getCreatedAt(),
                savedManager.getUpdatedAt()
        );
    }

}
