package com.sunfeax.citeria.dto.specialistservice;

import jakarta.annotation.Nullable;

public record SpecialistServicePatchRequestDto(
    @Nullable
    Long businessId,

    @Nullable
    Long specialistId,

    @Nullable
    Long serviceId
) {}
