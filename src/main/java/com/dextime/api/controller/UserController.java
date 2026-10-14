package com.dextime.api.controller;

import com.dextime.api.dto.UserPatchDto;
import com.dextime.api.dto.UserResponseDto;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /*
     * CRUD operations for users
     */

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> readUserByUsername(@AuthenticationPrincipal UserDetails loggedUser) {
        String username = loggedUser.getUsername();
        return ResponseEntity.ok(userService.readUserByUsername(username));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateUser(@AuthenticationPrincipal UserDetails loggedUser, @RequestBody @Valid UserPatchDto userPatchDto) {
        String username = loggedUser.getUsername();
        return ResponseEntity.ok(userService.updateUser(username, userPatchDto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails loggedUser) {
        String username = loggedUser.getUsername();
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
