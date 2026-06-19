package com.sunfeax.citeria.dto.specialistservice;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record SpecialistServicePostRequestDto(
    @NotNull(message = "Business id is required")
    UUID businessId,

    @NotNull(message = "Specialist id is required")
    UUID specialistId,

    @NotNull(message = "Service id is required")
    UUID serviceId
) {}
