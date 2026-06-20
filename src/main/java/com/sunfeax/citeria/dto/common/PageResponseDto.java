package com.sunfeax.citeria.dto.common;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Stable, frontend-facing pagination envelope. Unlike returning a raw Spring
 * {@code Page}, this shape will not change between Spring versions.
 */
public record PageResponseDto<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
