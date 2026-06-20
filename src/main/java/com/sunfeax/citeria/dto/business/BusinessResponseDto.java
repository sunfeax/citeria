package com.sunfeax.citeria.dto.business;

import java.time.Instant;
import java.util.UUID;

public record BusinessResponseDto(
    UUID id,
    String name,
    String description,
    String phone,
    String email,
    String website,
    String address,
    Boolean isActive,
    UUID ownerId,
    String ownerName,
    Instant createdAt,
    Instant updatedAt) {
}
