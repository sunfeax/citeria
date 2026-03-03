package com.sunfeax.citeria.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserChangePasswordRequestDto(
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^[A-Za-z@#$%^&+=!]+$",
        message = "Password must contain only Latin letters and allowed special characters (@#$%^&+=!)"
    )
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[@#$%^&+=!].*", message = "Password must contain at least one special character")
    @Pattern(regexp = "^\\S+$", message = "Password must not contain spaces")
    String newPassword
) {}
