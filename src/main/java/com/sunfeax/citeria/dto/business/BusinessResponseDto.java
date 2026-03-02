package com.sunfeax.citeria.dto.business;

import java.time.LocalDateTime;

public record BusinessResponseDto(
    Long id,
    String name,
    String description,
    String phone,
    String email,
    String website,
    String address,
    Boolean isActive,
    Long ownerId,
    String ownerName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
