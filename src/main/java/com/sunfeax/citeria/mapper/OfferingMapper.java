package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.offering.OfferingResponseDto;
import com.sunfeax.citeria.entity.OfferingEntity;

@Component
public class OfferingMapper {

    public OfferingResponseDto toResponseDto(OfferingEntity offeringEntity) {
        return new OfferingResponseDto(
            offeringEntity.getId(),
            offeringEntity.getBusiness().getId(),
            offeringEntity.getBusiness().getName(),
            offeringEntity.getSpecialist().getId(),
            offeringEntity.getSpecialist().getFirstName() + " " + offeringEntity.getSpecialist().getLastName(),
            offeringEntity.getService().getId(),
            offeringEntity.getService().getName(),
            offeringEntity.getPriceAmount(),
            offeringEntity.getDurationMinutes(),
            offeringEntity.isActive(),
            offeringEntity.getCreatedAt()
        );
    }
}
