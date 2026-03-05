package com.sunfeax.citeria.validation;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.mapper.SpecialistServiceMapper;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpecialistServiceValidator {

    private final SpecialistServiceRepository specialistServiceRepository;
    private final SpecialistServiceMapper specialistServiceMapper;

    public void validateRegister(
        SpecialistServicePostRequestDto request,
        BusinessEntity business,
        UserEntity specialist,
        ServiceEntity service
    ) {
        new ValidationResult()
            .addErrorIf(
                specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceId(
                    request.businessId(),
                    request.specialistId(),
                    request.serviceId()
                ),
                "specialistService",
                "Specialist service for the same business, specialist and service already exists"
            )
            .addErrorIf(specialist.getType() != UserType.SPECIALIST, "specialistId", "User must have SPECIALIST type")
            .addErrorIf(!specialist.isActive(), "specialistId", "Specialist must be active")
            .addErrorIf(!business.isActive(), "businessId", "Business must be active")
            .addErrorIf(!service.isActive(), "serviceId", "Service must be active")
            .addErrorIf(
                !service.getBusiness().getId().equals(business.getId()),
                "serviceId",
                "Service does not belong to provided business"
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(
        Long id,
        SpecialistServicePatchRequestDto request,
        BusinessEntity targetBusiness,
        UserEntity targetSpecialist,
        ServiceEntity targetService
    ) {
        new ValidationResult()
            .addErrorIf(!specialistServiceMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceIdAndIdNot(
                    targetBusiness.getId(),
                    targetSpecialist.getId(),
                    targetService.getId(),
                    id
                ),
                "specialistService",
                "Specialist service for the same business, specialist and service already exists"
            )
            .addErrorIf(targetSpecialist.getType() != UserType.SPECIALIST, "specialistId", "User must have SPECIALIST type")
            .addErrorIf(!targetSpecialist.isActive(), "specialistId", "Specialist must be active")
            .addErrorIf(!targetBusiness.isActive(), "businessId", "Business must be active")
            .addErrorIf(!targetService.isActive(), "serviceId", "Service must be active")
            .addErrorIf(
                !targetService.getBusiness().getId().equals(targetBusiness.getId()),
                "serviceId",
                "Service does not belong to provided business"
            )
            .throwIfHasErrors();
    }
}
