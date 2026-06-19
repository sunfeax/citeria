package com.sunfeax.citeria.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new UnauthorizedException("Authentication is required");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    public boolean isAdmin(UserEntity user) {
        return user.getRole() == UserRole.ADMIN;
    }

    /**
     * Grants access when the current user is the expected owner or an admin.
     * Throws {@link ForbiddenException} otherwise.
     */
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
