package com.sunfeax.citeria.dto.user;

import com.sunfeax.citeria.enums.UserType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    String firstName,

    @NotBlank(message = "Last name is required")
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
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "Password must contain at least one digit, one lowercase, one uppercase letter, and one special character"
    )
    String password,

    @NotNull(message = "Please select user type")
    UserType type
) {}
