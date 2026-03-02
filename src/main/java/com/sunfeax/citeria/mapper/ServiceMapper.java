package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.entity.ServiceEntity;

@Component
public class ServiceMapper {

    public ServiceResponseDto toResponseDto(ServiceEntity serviceEntity) {
        return new ServiceResponseDto(
            serviceEntity.getId(),
            serviceEntity.getName(),
            serviceEntity.getBusiness().getName(),
            serviceEntity.getDescription(),
            serviceEntity.getPriceAmount(),
            serviceEntity.getDurationMinutes(),
            serviceEntity.isActive(),
            serviceEntity.getCreatedAt()
        );
    }
}
