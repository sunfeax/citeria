package com.sunfeax.citeria.dto.user;

import java.time.Instant;

public record UserAvatarDto(
    String contentType,
    byte[] data,
    Instant updatedAt
) {}
