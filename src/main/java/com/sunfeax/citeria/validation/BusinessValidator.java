package com.sunfeax.citeria.validation;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.mapper.BusinessMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BusinessValidator {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    public void validateCreate(BusinessPostRequestDto request) {
        new ValidationResult()
            .addErrorIf(
                businessRepository.existsByNameIgnoreCase(request.name()),
                "name",
                "Business with name " + request.name() + " already exists"
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(Long id, BusinessEntity existingEntity, BusinessPatchRequestDto request) {
        String targetName = request.name() != null ? request.name() : existingEntity.getName();

        new ValidationResult()
            .addErrorIf(!businessMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                request.name() != null && businessRepository.existsByNameIgnoreCaseAndIdNot(targetName, id),
                "name",
                "Business with name " + targetName + " already exists"
            )
            .throwIfHasErrors();
    }
}
