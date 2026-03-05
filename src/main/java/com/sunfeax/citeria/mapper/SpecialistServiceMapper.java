package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;

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
}
