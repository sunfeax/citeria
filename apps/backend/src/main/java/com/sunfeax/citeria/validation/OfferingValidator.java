package com.sunfeax.citeria.validation;

import java.util.UUID;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.offering.OfferingPatchRequestDto;
import com.sunfeax.citeria.dto.offering.OfferingPostRequestDto;
import com.sunfeax.citeria.entity.OfferingEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.repository.OfferingRepository;
import com.sunfeax.citeria.mapper.OfferingMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OfferingValidator {

    private final OfferingRepository offeringRepository;
    private final OfferingMapper offeringMapper;

    public void validateCreate(OfferingPostRequestDto request, UserEntity specialist) {
        new ValidationResult()
            .addErrorIf(specialist.getType() != UserType.SPECIALIST, "specialist", "Only a specialist can offer services")
            .addErrorIf(
                offeringRepository.existsBySpecialistIdAndNameIgnoreCase(specialist.getId(), request.name()),
                "name",
                "You already have a service named " + request.name()
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(UUID id, OfferingEntity existingEntity, OfferingPatchRequestDto request) {
        String targetOfferingName = request.name() != null
            ? request.name()
            : existingEntity.getName();

        new ValidationResult()
            .addErrorIf(!offeringMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                offeringRepository.existsBySpecialistIdAndNameIgnoreCaseAndIdNot(
                    existingEntity.getSpecialist().getId(), targetOfferingName, id
                ),
                "name",
                "You already have a service named " + targetOfferingName
            )
            .throwIfHasErrors();
    }
}
