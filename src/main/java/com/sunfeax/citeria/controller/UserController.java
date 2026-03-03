package com.sunfeax.citeria.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserPatchRequestDto;
import com.sunfeax.citeria.dto.user.UserPostRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // change to Pageable after 
    @GetMapping
    public List<UserResponseDto> getUsers() {
        return userService.getAll();
    }

    // fetch user by id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // register new user
    @PostMapping
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserPostRequestDto request) {
        UserResponseDto response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // soft delete (isActive = 0)
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDto> deactivateById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivateById(id));
    }

    // hard delete from DB
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<UserResponseDto> hardDeleteById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.hardDeleteById(id));
    }

    // update profile fields for an existing user
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
        @PathVariable Long id,
        @Valid @RequestBody UserPatchRequestDto request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // change password for an existing user
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
        @PathVariable Long id,
        @Valid @RequestBody UserChangePasswordRequestDto request
    ) {
        userService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }
}
