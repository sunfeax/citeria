package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;

@Component
public class ServiceMapper {

    public ServiceResponseDto toResponseDto(ServiceEntity serviceEntity) {
        return new ServiceResponseDto(
            serviceEntity.getId(),
            serviceEntity.getBusiness().getId(),
            serviceEntity.getName(),
            serviceEntity.getBusiness().getName(),
            serviceEntity.getDescription(),
            serviceEntity.getPriceAmount(),
            serviceEntity.getDurationMinutes(),
            serviceEntity.getCurrency(),
            serviceEntity.isActive(),
            serviceEntity.getCreatedAt()
        );
    }

    public ServiceEntity createEntity(ServicePostRequestDto request, BusinessEntity business) {
        ServiceEntity entity = new ServiceEntity();

        entity.setBusiness(business);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setPriceAmount(request.priceAmount());
        entity.setCurrency(request.currency());

        return entity;
    }

    public ServiceEntity applyPatch(ServiceEntity entity, ServicePatchRequestDto request, BusinessEntity business) {
        if (business != null) {
            entity.setBusiness(business);
        }
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

    public boolean hasAnyPatchField(ServicePatchRequestDto request) {
        return request.businessId() != null
            || request.name() != null
            || request.description() != null
            || request.durationMinutes() != null
            || request.priceAmount() != null
            || request.currency() != null;
    }
}
