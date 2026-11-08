package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.offering.OfferingPatchRequestDto;
import com.sunfeax.citeria.dto.offering.OfferingPostRequestDto;
import com.sunfeax.citeria.dto.offering.OfferingResponseDto;
import com.sunfeax.citeria.entity.OfferingEntity;
import com.sunfeax.citeria.entity.UserEntity;

@Component
public class OfferingMapper {

    public OfferingResponseDto toResponseDto(OfferingEntity offeringEntity) {
        return new OfferingResponseDto(
            offeringEntity.getId(),
            offeringEntity.getSpecialist().getId(),
            offeringEntity.getSpecialist().getFirstName() + " " + offeringEntity.getSpecialist().getLastName(),
            offeringEntity.getSpecialist().getAvatarVersion(),
            offeringEntity.getName(),
            offeringEntity.getDescription(),
            offeringEntity.getPriceAmount(),
            offeringEntity.getDurationMinutes(),
            offeringEntity.getCurrency(),
            offeringEntity.isActive(),
            offeringEntity.getCreatedAt()
        );
    }

    public OfferingEntity createEntity(OfferingPostRequestDto request, UserEntity specialist) {
        OfferingEntity entity = new OfferingEntity();

        entity.setSpecialist(specialist);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setPriceAmount(request.priceAmount());
        entity.setCurrency(request.currency());

        return entity;
    }

    public OfferingEntity applyPatch(OfferingEntity entity, OfferingPatchRequestDto request) {
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.durationMinutes() != null) {
            entity.setDurationMinutes(request.durationMinutes());
        }
        if (request.priceAmount() != null) {
            entity.setPriceAmount(request.priceAmount());
        }
        if (request.currency() != null) {
            entity.setCurrency(request.currency());
        }

        return entity;
    }

    public boolean hasAnyPatchField(OfferingPatchRequestDto request) {
        return request.name() != null
            || request.description() != null
            || request.durationMinutes() != null
            || request.priceAmount() != null
            || request.currency() != null;
    }
}
