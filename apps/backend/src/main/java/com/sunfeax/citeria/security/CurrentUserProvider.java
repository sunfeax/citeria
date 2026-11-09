package com.sunfeax.citeria.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Optional;

import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.exception.ForbiddenException;
import com.sunfeax.citeria.exception.UnauthorizedException;
import com.sunfeax.citeria.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        return findCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
    }

    public Optional<UserEntity> findCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated() || authentication.getName() == null) {
            return Optional.empty();
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email);
    }

    public boolean isAdmin(UserEntity user) {
        return user.getRole() == UserRole.ADMIN;
    }

    public UserEntity requireSelfOrAdmin(UUID ownerId) {
        UserEntity current = getCurrentUser();
        if (!isAdmin(current) && !current.getId().equals(ownerId)) {
            throw new ForbiddenException("You do not have permission to access this resource");
        }
        return current;
    }

    public UserEntity requireAdmin() {
        UserEntity current = getCurrentUser();
        if (!isAdmin(current)) {
            throw new ForbiddenException("Administrator privileges are required");
        }
        return current;
    }
}
