package com.sunfeax.citeria.dto.specialistservice;

import java.util.UUID;
import jakarta.annotation.Nullable;

public record SpecialistServicePatchRequestDto(
    @Nullable
    UUID businessId,

    @Nullable
    UUID specialistId,

    @Nullable
    UUID serviceId
) {}
