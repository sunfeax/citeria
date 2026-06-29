package com.sunfeax.citeria.validation;

import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.workinghours.WorkingHoursPatchRequestDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursPostRequestDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.entity.WorkingHoursEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.mapper.WorkingHoursMapper;
import com.sunfeax.citeria.repository.WorkingHoursRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WorkingHoursValidator {

    private final WorkingHoursRepository workingHoursRepository;
    private final WorkingHoursMapper workingHoursMapper;

    public void validateCreate(WorkingHoursPostRequestDto request, UserEntity specialist) {
        new ValidationResult()
            .addErrorIf(specialist.getType() != UserType.SPECIALIST, "specialist", "Only a specialist can define working hours")
            .addErrorIf(
                !request.endTime().isAfter(request.startTime()),
                "time",
                "End time must be after start time"
            )
            .addErrorIf(
                workingHoursRepository.existsBySpecialistIdAndDayOfWeek(specialist.getId(), request.dayOfWeek()),
                "dayOfWeek",
                "Working hours for this day already exist"
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(WorkingHoursEntity existingEntity, WorkingHoursPatchRequestDto request) {
        LocalTime targetStartTime = request.startTime() != null ? request.startTime() : existingEntity.getStartTime();
        LocalTime targetEndTime = request.endTime() != null ? request.endTime() : existingEntity.getEndTime();

        new ValidationResult()
            .addErrorIf(!workingHoursMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                !targetEndTime.isAfter(targetStartTime),
                "time",
                "End time must be after start time"
            )
            .throwIfHasErrors();
    }
}
