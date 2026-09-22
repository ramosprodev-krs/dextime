package com.dextime.api.controller;

import com.dextime.api.dto.UserPatchDto;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.service.UserService;
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
    public UserEntity readUserByUsername(@AuthenticationPrincipal UserDetails loggedUser) {
        String username = loggedUser.getUsername();
        return userService.readUserByUsername(username);
    }

    @PatchMapping("/me")
    public UserEntity updateUser(@AuthenticationPrincipal UserDetails loggedUser, @RequestBody UserPatchDto userPatchDto) {
        String username = loggedUser.getUsername();
        return userService.updateUser(username, userPatchDto);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails loggedUser) {
        String username = loggedUser.getUsername();
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
