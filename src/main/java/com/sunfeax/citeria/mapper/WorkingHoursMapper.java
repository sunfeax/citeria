package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.workinghours.WorkingHoursPatchRequestDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursPostRequestDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.entity.WorkingHoursEntity;

@Component
public class WorkingHoursMapper {

    public WorkingHoursResponseDto toResponseDto(WorkingHoursEntity entity) {
        return new WorkingHoursResponseDto(
            entity.getId(),
            entity.getSpecialist().getId(),
            entity.getSpecialist().getFirstName() + " " + entity.getSpecialist().getLastName(),
            entity.getBusiness().getId(),
            entity.getBusiness().getName(),
            entity.getDayOfWeek(),
            entity.getStartTime(),
            entity.getEndTime(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }

    public WorkingHoursEntity createEntity(
        WorkingHoursPostRequestDto request,
        BusinessEntity business,
        UserEntity specialist
    ) {
        WorkingHoursEntity entity = new WorkingHoursEntity();

        entity.setBusiness(business);
        entity.setSpecialist(specialist);
        entity.setDayOfWeek(request.dayOfWeek());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setActive(true);

        return entity;
    }

    public WorkingHoursEntity applyPatch(WorkingHoursEntity entity, WorkingHoursPatchRequestDto request) {
        if (request.startTime() != null) {
            entity.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            entity.setEndTime(request.endTime());
        }
        if (request.isActive() != null) {
            entity.setActive(request.isActive());
        }

        return entity;
    }

    public boolean hasAnyPatchField(WorkingHoursPatchRequestDto request) {
        return request.startTime() != null
            || request.endTime() != null
            || request.isActive() != null;
    }
}
