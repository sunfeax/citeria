package com.sunfeax.citeria.normalizer;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.user.UserPatchRequestDto;
import com.sunfeax.citeria.dto.user.UserPostRequestDto;

@Component
public class UserFieldNormalizer {

    public UserPostRequestDto normalizePostRequest(UserPostRequestDto request) {
        return new UserPostRequestDto(
            normalizeName(request.firstName()),
            normalizeName(request.lastName()),
            normalizeEmail(request.email()),
            normalizePhone(request.phone()),
            request.password(),
            request.type()
        );
    }

    public UserPatchRequestDto normalizePatchRequest(UserPatchRequestDto request) {
        return new UserPatchRequestDto(
            normalizeName(request.firstName()),
            normalizeName(request.lastName()),
            normalizeEmail(request.email()),
            normalizePhone(request.phone()),
            request.type()
        );
    }

    public String normalizeName(String value) {
        if (value == null || (value = value.trim()).isEmpty()) {
            return value;
        }
        value = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    public String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizePhone(String value) {
        return value == null ? null : value.replaceAll("[+()\\s\\p{Pd}]", "");
    }
}
