package com.sunfeax.citeria.normalizer;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserUpdateRequestDto;

@Component
public class UserFieldNormalizer {

    public RegisterRequestDto normalizePostRequest(RegisterRequestDto request) {
        return new RegisterRequestDto(
            normalizeName(request.firstName()),
            normalizeName(request.lastName()),
            normalizeEmail(request.email()),
            normalizePhone(request.phone()),
            request.password(),
            request.type()
        );
    }

    public UserUpdateRequestDto normalizePatchRequest(UserUpdateRequestDto request) {
        return new UserUpdateRequestDto(
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
