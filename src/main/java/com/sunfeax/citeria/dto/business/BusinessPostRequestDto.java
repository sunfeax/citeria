package com.sunfeax.citeria.dto.business;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BusinessPostRequestDto(
    @NotNull(message = "Owner id is required")
    Long ownerId,

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 120)
    String name,

    @Nullable
    @Size(max = 1000)
    String description,

    @Nullable
    @Size(min = 7, max = 20)
    String phone,

    @Nullable
    @Email(message = "Invalid email format")
    @Size(max = 100)
    String email,

    @Nullable
    @Size(max = 255)
    String website,

    @Nullable
    @Size(max = 255)
    String address
) {}
