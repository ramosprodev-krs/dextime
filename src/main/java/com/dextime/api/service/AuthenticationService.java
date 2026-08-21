package com.dextime.api.service;

import com.dextime.api.entity.CustomerSnapshot;
import com.dextime.api.entity.ProviderSnapshot;
import com.dextime.api.entity.Role;
import com.dextime.api.exception.BusinessException;
import com.dextime.api.repository.CustomerSnapshotRepository;
import com.dextime.api.repository.ProviderSnapshotRepository;
import com.dextime.api.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final CustomerSnapshotRepository customerSnapshotRepository;
    private final ProviderSnapshotRepository providerSnapshotRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthenticationService(CustomerSnapshotRepository customerSnapshotRepository,
                                   ProviderSnapshotRepository providerSnapshotRepository,
                                   PasswordEncoder passwordEncoder,
                                   JwtUtil jwtUtil) {
        this.customerSnapshotRepository = customerSnapshotRepository;
        this.providerSnapshotRepository = providerSnapshotRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String email, String password) {
        Optional<CustomerSnapshot> customerOptional = customerSnapshotRepository.findByEmail(email);
        
        if (customerOptional.isEmpty()) {
            throw new BusinessException("User not found");
        }
        
        CustomerSnapshot customer = customerOptional.get();
        
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new BusinessException("Invalid password");
        }
        
        return jwtUtil.generateToken(email, customer.getRole().name());
    }

    public String register(String firstName, String lastName, String email, String phone, String password, String role) {
        if (customerSnapshotRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email already exists");
        }
        
        Role userRole = Role.valueOf(role.toUpperCase());
        
        CustomerSnapshot customer = new CustomerSnapshot();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setRole(userRole);
        
        CustomerSnapshot savedCustomer = customerSnapshotRepository.save(customer);
        
        if (userRole == Role.PROVIDER) {
            ProviderSnapshot provider = new ProviderSnapshot();
            provider.setUserId(savedCustomer.getId());
            provider.setName(firstName + " " + lastName);
            provider.setPhone(phone);
            providerSnapshotRepository.save(provider);
        }
        
        return jwtUtil.generateToken(email, userRole.name());
    }
}
