package com.sunfeax.citeria.validation;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;

@Component
public class ServiceFieldNormalizer {

    public ServicePostRequestDto normalizePostRequest(ServicePostRequestDto request) {
        return new ServicePostRequestDto(
            request.businessId(),
            normalizeText(request.name()),
            normalizeText(request.description()),
            request.durationMinutes(),
            request.priceAmount(),
            normalizeCurrency(request.currency())
        );
    }

    public ServicePatchRequestDto normalizePatchRequest(ServicePatchRequestDto request) {
        return new ServicePatchRequestDto(
            request.businessId(),
            normalizeText(request.name()),
            normalizeText(request.description()),
            request.durationMinutes(),
            request.priceAmount(),
            normalizeCurrency(request.currency())
        );
    }

    public String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    public String normalizeCurrency(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
