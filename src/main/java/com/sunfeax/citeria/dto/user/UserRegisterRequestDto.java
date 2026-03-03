package com.sunfeax.citeria.dto.user;

import com.sunfeax.citeria.enums.UserType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(
    @NotBlank(message = "First name is required")
    @Pattern(
        regexp = "^[\\p{L}]+(?:[-' ][\\p{L}]+)*$",
        message = "First name must contain letters only"
    )
    @Size(min = 2, max = 50)
    String firstName,

    @NotBlank(message = "Last name is required")
    @Pattern(
        regexp = "^[\\p{L}]+(?:[-' ][\\p{L}]+)*$",
        message = "Last name must contain letters only"
    )
    @Size(min = 2, max = 50)
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(min = 5, max = 100)
    String email,

    @NotBlank(message = "Phone is required")
    @Size(min = 7, max = 20)
    String phone,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^[A-Za-z@#$%^&+=!]+$",
        message = "Password must contain only Latin letters and allowed special characters (@#$%^&+=!)"
    )
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[@#$%^&+=!].*", message = "Password must contain at least one special character")
    @Pattern(regexp = "^\\S+$", message = "Password must not contain spaces")
    String password,

    @NotNull(message = "Please select user type")
    UserType type
) {}
