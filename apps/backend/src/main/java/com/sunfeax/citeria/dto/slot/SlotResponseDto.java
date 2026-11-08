package com.sunfeax.citeria.dto.slot;

import java.time.Instant;

public record SlotResponseDto(
    Instant startTime,
    Instant endTime
) {}
