package com.sunfeax.citeria.normalizer;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.workinghours.WorkingHoursPatchRequestDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursPostRequestDto;

@Component
public class WorkingHoursFieldNormalizer {

    public WorkingHoursPostRequestDto normalizePostRequest(WorkingHoursPostRequestDto request) {
        return new WorkingHoursPostRequestDto(
            request.dayOfWeek(),
            normalizeTime(request.startTime()),
            normalizeTime(request.endTime())
        );
    }

    public WorkingHoursPatchRequestDto normalizePatchRequest(WorkingHoursPatchRequestDto request) {
        return new WorkingHoursPatchRequestDto(
            normalizeTime(request.startTime()),
            normalizeTime(request.endTime()),
            request.isActive()
        );
    }

    private LocalTime normalizeTime(LocalTime value) {
        return value == null ? null : value.truncatedTo(ChronoUnit.MINUTES);
    }
}
