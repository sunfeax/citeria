package com.sunfeax.citeria.util;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Restricts client-supplied sorting to a whitelist of sortable properties, so
 * callers cannot sort by arbitrary (or non-existent) entity fields.
 */
public final class PageableUtil {

    private PageableUtil() {
    }

    public static Pageable sanitizeSort(Pageable pageable, Set<String> allowedProperties, Sort fallback) {
        List<Sort.Order> safeOrders = pageable.getSort().stream()
            .filter(order -> allowedProperties.contains(order.getProperty()))
            .toList();

        Sort sort = safeOrders.isEmpty() ? fallback : Sort.by(safeOrders);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
