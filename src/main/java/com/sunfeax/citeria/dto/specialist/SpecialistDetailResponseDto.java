package com.sunfeax.citeria.dto.specialist;

import java.util.List;
import java.util.UUID;

import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursResponseDto;

public record SpecialistDetailResponseDto(
    UUID id,
    String firstName,
    String lastName,
    Boolean isActive,
    List<ServiceResponseDto> services,
    List<WorkingHoursResponseDto> workingHours
) {}
