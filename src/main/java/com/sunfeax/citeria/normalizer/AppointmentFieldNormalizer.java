package com.sunfeax.citeria.normalizer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;

@Component
public class AppointmentFieldNormalizer {

    public AppointmentPostRequestDto normalizePostRequest(AppointmentPostRequestDto request) {
        return new AppointmentPostRequestDto(
            request.specialistServiceId(),
            normalizeDateTime(request.startTime()),
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

    private Instant normalizeDateTime(Instant value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MINUTES);
    }
}
