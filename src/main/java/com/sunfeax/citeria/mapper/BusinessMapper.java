package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;

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
}
