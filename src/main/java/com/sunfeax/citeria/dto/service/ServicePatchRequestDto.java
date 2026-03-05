package com.sunfeax.citeria.dto.service;

import java.math.BigDecimal;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ServicePatchRequestDto(
    @Nullable
    Long businessId,

    @Nullable
    @Pattern(regexp = ".*\\S.*", message = "Service name must not be empty")
    @Size(min = 3, max = 120)
    String name,

    @Nullable
    @Size(max = 500)
    String description,

    @Nullable
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 480, message = "Duration must not exceed 480 minutes")
    Integer durationMinutes,

    @Nullable
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    BigDecimal priceAmount,

    @Nullable
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter code")
    String currency
) {}
