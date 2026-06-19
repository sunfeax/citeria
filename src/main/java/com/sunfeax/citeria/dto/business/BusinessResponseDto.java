package com.sunfeax.citeria.dto.business;

import java.util.UUID;
import java.time.LocalDateTime;

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
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
