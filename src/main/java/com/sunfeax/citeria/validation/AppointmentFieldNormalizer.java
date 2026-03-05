package com.sunfeax.citeria.validation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;

@Component
public class AppointmentFieldNormalizer {

    public AppointmentPostRequestDto normalizePostRequest(AppointmentPostRequestDto request) {
        return new AppointmentPostRequestDto(
            request.clientId(),
            request.specialistServiceId(),
            normalizeDateTime(request.startTime()),
            normalizeDateTime(request.endTime()),
            request.paymentMethod()
        );
    }

    public AppointmentPatchRequestDto normalizePatchRequest(AppointmentPatchRequestDto request) {
        return new AppointmentPatchRequestDto(
            request.clientId(),
            request.specialistServiceId(),
            normalizeDateTime(request.startTime()),
            normalizeDateTime(request.endTime()),
            request.status(),
            request.paymentMethod()
        );
    }

    private LocalDateTime normalizeDateTime(LocalDateTime value) {
        return value == null ? null : value.withSecond(0).withNano(0);
    }
}
