package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;
import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.UserEntity;

@Component
public class BusinessMapper {

    public BusinessResponseDto toResponseDto(BusinessEntity businessEntity) {
        return new BusinessResponseDto(
            businessEntity.getId(),
            businessEntity.getName(),
            businessEntity.getDescription(),
            businessEntity.getPhone(),
            businessEntity.getEmail(),
            businessEntity.getWebsite(),
            businessEntity.getAddress(),
            businessEntity.isActive(),
            businessEntity.getOwner().getId(),
            businessEntity.getOwner().getFirstName() + " " + businessEntity.getOwner().getLastName(),
            businessEntity.getCreatedAt(),
            businessEntity.getUpdatedAt()
        );
    }

    public BusinessEntity createEntity(BusinessPostRequestDto request, UserEntity owner) {
        BusinessEntity entity = new BusinessEntity();

        entity.setOwner(owner);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setWebsite(request.website());
        entity.setAddress(request.address());

        return entity;
    }

    public BusinessEntity applyPatch(BusinessEntity entity, BusinessPatchRequestDto request, UserEntity owner) {
        if (owner != null) {
            entity.setOwner(owner);
        }
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.phone() != null) {
            entity.setPhone(request.phone());
        }
        if (request.email() != null) {
            entity.setEmail(request.email());
        }
        if (request.website() != null) {
            entity.setWebsite(request.website());
        }
        if (request.address() != null) {
            entity.setAddress(request.address());
        }

        return entity;
    }

    public boolean hasAnyPatchField(BusinessPatchRequestDto request) {
        return request.ownerId() != null
            || request.name() != null
            || request.description() != null
            || request.phone() != null
            || request.email() != null
            || request.website() != null
            || request.address() != null;
    }
}
