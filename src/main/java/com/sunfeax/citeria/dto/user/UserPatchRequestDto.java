package com.sunfeax.citeria.dto.user;

import com.sunfeax.citeria.enums.UserType;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserPatchRequestDto(
    @Nullable
    @Pattern(
        regexp = "^\\s*[\\p{L}]+(?:[-' ][\\p{L}]+)*\\s*$",
        message = "First name must contain letters only"
    )
    @Size(min = 2, max = 50)
    String firstName,

    @Nullable
    @Pattern(
        regexp = "^\\s*[\\p{L}]+(?:[-' ][\\p{L}]+)*\\s*$",
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
    UserType type
) {}
