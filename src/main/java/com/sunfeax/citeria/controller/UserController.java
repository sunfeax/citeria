package com.sunfeax.citeria.controller;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.dto.user.UserUpdateRequestDto;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public PageResponseDto<UserResponseDto> list(
        @RequestParam(required = false) UserRole role,
        @RequestParam(required = false) UserType type,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return userService.list(role, type, active, search, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(Authentication auth) {
        String email = auth.getName();

        return ResponseEntity.ok(userService.getMe(email));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody UserUpdateRequestDto request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
        @PathVariable UUID id,
        @Valid @RequestBody UserChangePasswordRequestDto request
    ) {
        userService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDto> deactivateById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivateById(id));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<UserResponseDto> hardDeleteById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.hardDeleteById(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<UserResponseDto> restoreById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.restoreById(id));
    }
}
