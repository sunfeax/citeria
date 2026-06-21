package com.sunfeax.citeria.validation;

import java.util.UUID;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.mapper.ServiceMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceValidator {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    public void validateCreate(ServicePostRequestDto request, UserEntity specialist) {
        new ValidationResult()
            .addErrorIf(specialist.getType() != UserType.SPECIALIST, "specialist", "Only a specialist can offer services")
            .addErrorIf(
                serviceRepository.existsBySpecialistIdAndNameIgnoreCase(specialist.getId(), request.name()),
                "name",
                "You already have a service named " + request.name()
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(UUID id, ServiceEntity existingEntity, ServicePatchRequestDto request) {
        String targetServiceName = request.name() != null
            ? request.name()
            : existingEntity.getName();

        new ValidationResult()
            .addErrorIf(!serviceMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                serviceRepository.existsBySpecialistIdAndNameIgnoreCaseAndIdNot(
                    existingEntity.getSpecialist().getId(), targetServiceName, id
                ),
                "name",
                "You already have a service named " + targetServiceName
            )
            .throwIfHasErrors();
    }
}
