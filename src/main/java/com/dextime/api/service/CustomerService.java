package com.dextime.api.service;

import com.dextime.api.dto.UserDto;
import com.dextime.api.dto.UserResponseDto;
import com.dextime.api.entity.*;
import com.dextime.api.exception.*;
import com.dextime.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(UserRepository userRepository, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * Following operations for customers
     */

    public UserResponseDto createCustomer(UserDto userDto) {
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

        UserEntity customer = new UserEntity();
        customer.setUsername(userDto.getUsername());
        customer.setPassword(passwordEncoder.encode(userDto.getPassword()));
        customer.setFirstName(userDto.getFirstName());
        customer.setLastName(userDto.getLastName());
        customer.setEmail(userDto.getEmail());
        customer.setPhoneNumber(userDto.getPhoneNumber());
        customer.setCpf(userDto.getCpf());
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        customer.setRole(UserRole.CUSTOMER);

        UserEntity savedCustomer = userRepository.save(customer);

        return new UserResponseDto(
                savedCustomer.getId(),
                savedCustomer.getUsername(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getEmail(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getCpf(),
                savedCustomer.getRole(),
                savedCustomer.getCreatedAt(),
                savedCustomer.getUpdatedAt()
        );

    }

}
