package com.sunfeax.citeria.normalizer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;

@Component
public class AppointmentFieldNormalizer {

    public AppointmentPostRequestDto normalizePostRequest(AppointmentPostRequestDto request) {
        return new AppointmentPostRequestDto(
            request.serviceId(),
            normalizeDateTime(request.startTime())
        );
    }

    private Instant normalizeDateTime(Instant value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MINUTES);
    }
}
