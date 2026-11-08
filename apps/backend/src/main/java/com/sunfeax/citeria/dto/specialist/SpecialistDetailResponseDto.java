package com.sunfeax.citeria.dto.specialist;

import java.util.List;
import java.util.UUID;

import com.sunfeax.citeria.dto.offering.OfferingResponseDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursResponseDto;

public record SpecialistDetailResponseDto(
    UUID id,
    String firstName,
    String lastName,
    Boolean isActive,
    List<OfferingResponseDto> offerings,
    List<WorkingHoursResponseDto> workingHours
) {}
