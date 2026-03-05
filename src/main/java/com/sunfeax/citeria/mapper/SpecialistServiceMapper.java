package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;

@Component
public class SpecialistServiceMapper {

    public SpecialistServiceResponseDto toResponseDto(SpecialistServiceEntity specialistServiceEntity) {
        return new SpecialistServiceResponseDto(
            specialistServiceEntity.getId(),
            specialistServiceEntity.getBusiness().getId(),
            specialistServiceEntity.getBusiness().getName(),
            specialistServiceEntity.getSpecialist().getId(),
            specialistServiceEntity.getSpecialist().getFirstName() + " " + specialistServiceEntity.getSpecialist().getLastName(),
            specialistServiceEntity.getService().getId(),
            specialistServiceEntity.getService().getName(),
            specialistServiceEntity.isActive(),
            specialistServiceEntity.getCreatedAt()
        );
    }

    public SpecialistServiceEntity createEntity(
        SpecialistServicePostRequestDto request,
        BusinessEntity business,
        UserEntity specialist,
        ServiceEntity service
    ) {
        SpecialistServiceEntity entity = new SpecialistServiceEntity();

        entity.setBusiness(business);
        entity.setSpecialist(specialist);
        entity.setService(service);

        return entity;
    }

    public SpecialistServiceEntity applyPatch(
        SpecialistServiceEntity entity,
        SpecialistServicePatchRequestDto request,
        BusinessEntity business,
        UserEntity specialist,
        ServiceEntity service
    ) {
        if (business != null) {
            entity.setBusiness(business);
        }
        if (specialist != null) {
            entity.setSpecialist(specialist);
        }
        if (service != null) {
            entity.setService(service);
        }

        return entity;
    }

    public boolean hasAnyPatchField(SpecialistServicePatchRequestDto request) {
        return request.businessId() != null || request.specialistId() != null || request.serviceId() != null;
    }
}
