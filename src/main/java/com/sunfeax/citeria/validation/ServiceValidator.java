package com.sunfeax.citeria.validation;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.mapper.ServiceMapper;
import com.sunfeax.citeria.repository.ServiceRepository;

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

    public void validateUpdate(Long id, ServicePatchRequestDto request, Long targetBusinessId, String targetServiceName) {
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
