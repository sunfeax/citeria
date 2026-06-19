package com.sunfeax.citeria.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.UnauthorizedException;
import com.sunfeax.citeria.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new UnauthorizedException("Authentication is required");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }
}
