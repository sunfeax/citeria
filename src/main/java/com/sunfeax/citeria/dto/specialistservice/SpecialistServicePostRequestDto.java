package com.sunfeax.citeria.dto.specialistservice;

import jakarta.validation.constraints.NotNull;

public record SpecialistServicePostRequestDto(
    @NotNull(message = "Business id is required")
    Long businessId,

    @NotNull(message = "Specialist id is required")
    Long specialistId,

    @NotNull(message = "Service id is required")
    Long serviceId
) {}
