package com.sunfeax.citeria.normalizer;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.offering.OfferingPatchRequestDto;
import com.sunfeax.citeria.dto.offering.OfferingPostRequestDto;

@Component
public class OfferingFieldNormalizer {

    public OfferingPostRequestDto normalizePostRequest(OfferingPostRequestDto request) {
        return new OfferingPostRequestDto(
            normalizeText(request.name()),
            normalizeText(request.description()),
            request.durationMinutes(),
            request.priceAmount(),
            normalizeCurrency(request.currency())
        );
    }

    public OfferingPatchRequestDto normalizePatchRequest(OfferingPatchRequestDto request) {
        return new OfferingPatchRequestDto(
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
