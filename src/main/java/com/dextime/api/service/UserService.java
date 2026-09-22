package com.dextime.api.service;

import com.dextime.api.dto.UserPatchDto;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE + username));
    }

    /*
     * Following CRUD operations for customers
     */


    public UserEntity readUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE + username));
    }

    public UserEntity updateUser(String username, UserPatchDto userPatchDto) {
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE + username));

        if (userPatchDto.getUsername() != null) {
            user.setUsername(userPatchDto.getUsername());
        }
        if (userPatchDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userPatchDto.getPassword()));
        }
        if (userPatchDto.getEmail() != null) {
            user.setEmail(userPatchDto.getEmail());
        }
        if (userPatchDto.getPhoneNumber() != null) {
            user.setPhoneNumber(userPatchDto.getPhoneNumber());
        }

        return userRepository.save(user);
    }

    public void deleteUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE+ username));

        userRepository.delete(user);
    }

}
