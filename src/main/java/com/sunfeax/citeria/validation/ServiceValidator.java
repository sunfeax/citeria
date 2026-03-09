package com.sunfeax.citeria.validation;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.mapper.ServiceMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceValidator {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    public void validateCreate(ServicePostRequestDto request) {
        new ValidationResult()
            .addErrorIf(
                serviceRepository.existsByBusinessIdAndNameIgnoreCase(request.businessId(), request.name()),
                "name",
                "Service with name " + request.name() + " already exists in this business"
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(Long id, ServiceEntity existingEntity, ServicePatchRequestDto request) {
        Long targetBusinessId = request.businessId() != null
            ? request.businessId()
            : existingEntity.getBusiness().getId();
        String targetServiceName = request.name() != null
            ? request.name()
            : existingEntity.getName();

        new ValidationResult()
            .addErrorIf(!serviceMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                serviceRepository.existsByBusinessIdAndNameIgnoreCaseAndIdNot(targetBusinessId, targetServiceName, id),
                "name",
                "Service with name " + targetServiceName + " already exists in this business"
            )
            .throwIfHasErrors();
    }
}
