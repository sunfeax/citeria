package com.sunfeax.citeria.validation;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;

@Component
public class BusinessFieldNormalizer {

    public BusinessPostRequestDto normalizePostRequest(BusinessPostRequestDto request) {
        return new BusinessPostRequestDto(
            request.ownerId(),
            normalizeText(request.name()),
            normalizeText(request.description()),
            normalizePhone(request.phone()),
            normalizeEmail(request.email()),
            normalizeText(request.website()),
            normalizeText(request.address())
        );
    }

    public BusinessPatchRequestDto normalizePatchRequest(BusinessPatchRequestDto request) {
        return new BusinessPatchRequestDto(
            request.ownerId(),
            normalizeText(request.name()),
            normalizeText(request.description()),
            normalizePhone(request.phone()),
            normalizeEmail(request.email()),
            normalizeText(request.website()),
            normalizeText(request.address())
        );
    }

    public String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    public String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizePhone(String value) {
        return value == null ? null : value.replaceAll("[+()\\s\\p{Pd}]", "");
    }
}
