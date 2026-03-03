package com.sunfeax.citeria.normalizer;

import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.user.UserRegisterRequestDto;

@Component
public class UserFieldNormalizer {

    public UserRegisterRequestDto normalizeRequest(UserRegisterRequestDto request) {
        return new UserRegisterRequestDto(
            normalizeName(request.firstName()),
            normalizeName(request.lastName()),
            normalizeEmail(request.email()),
            normalizePhone(request.phone()),
            request.password(),
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
        Objects.requireNonNull(value, "Email cannot be null");
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizePhone(String value) {
        return value == null
            ? null
            : value.replaceAll("[+()\\s\\p{Pd}]", "");
    }
}
