package com.sunfeax.citeria.dto.user;

import com.sunfeax.citeria.enums.UserType;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserPatchRequestDto(
    @Nullable
    @Pattern(
        regexp = "^[\\p{L}]+(?:[-' ][\\p{L}]+)*$",
        message = "First name must contain letters only"
    )
    @Size(min = 2, max = 50)
    String firstName,

    @Nullable
    @Pattern(
        regexp = "^[\\p{L}]+(?:[-' ][\\p{L}]+)*$",
        message = "Last name must contain letters only"
    )
    @Size(min = 2, max = 50)
    String lastName,

    @Nullable
    @Email(message = "Invalid email format")
    @Size(min = 5, max = 100)
    String email,

    @Nullable
    @Size(min = 7, max = 20)
    String phone,

    @Nullable
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^[A-Za-z@#$%^&+=!]+$",
        message = "Password must contain only Latin letters and allowed special characters (@#$%^&+=!)"
    )
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[@#$%^&+=!].*", message = "Password must contain at least one special character")
    @Pattern(regexp = "^\\S+$", message = "Password must not contain spaces")
    String password,
    
    @Nullable
    UserType type
) {}
