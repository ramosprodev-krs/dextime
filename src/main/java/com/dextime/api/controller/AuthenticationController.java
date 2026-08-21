package com.dextime.api.controller;

import com.dextime.api.dto.AuthResponse;
import com.dextime.api.dto.LoginRequest;
import com.dextime.api.dto.RegisterRequest;
import com.dextime.api.service.AuthenticationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        String token = authenticationService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return new AuthResponse(token);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest registerRequest) {
        String token = authenticationService.register(
                registerRequest.getFirstName(),
                registerRequest.getLastName(),
                registerRequest.getEmail(),
                registerRequest.getPhone(),
                registerRequest.getPassword(),
                registerRequest.getRole()
        );
        return new AuthResponse(token);
    }
}
