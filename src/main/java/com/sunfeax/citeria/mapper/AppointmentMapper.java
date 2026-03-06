package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;

@Component
public class AppointmentMapper {

    public AppointmentResponseDto toResponseDto(AppointmentEntity appointmentEntity) {
        return new AppointmentResponseDto(
            appointmentEntity.getId(),
            appointmentEntity.getClient().getId(),
            appointmentEntity.getClient().getFirstName() + " " + appointmentEntity.getClient().getLastName(),
            appointmentEntity.getClient().getEmail(),
            appointmentEntity.getSpecialistService().getId(),
            appointmentEntity.getSpecialistService().getSpecialist().getId(),
            appointmentEntity.getSpecialistService().getSpecialist().getFirstName() + " "
                + appointmentEntity.getSpecialistService().getSpecialist().getLastName(),
            appointmentEntity.getSpecialistService().getService().getId(),
            appointmentEntity.getSpecialistService().getService().getName(),
            appointmentEntity.getSpecialistService().getBusiness().getName(),
            appointmentEntity.getStartTime(),
            appointmentEntity.getEndTime(),
            appointmentEntity.getStatus(),
            appointmentEntity.getPaymentMethod(),
            appointmentEntity.getPriceAmount()
        );
    }

    public AppointmentEntity createEntity(
        AppointmentPostRequestDto request,
        UserEntity client,
        SpecialistServiceEntity specialistService
    ) {
        AppointmentEntity entity = new AppointmentEntity();

        entity.setClient(client);
        entity.setSpecialist(specialistService.getSpecialist());
        entity.setSpecialistService(specialistService);
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setPaymentMethod(request.paymentMethod());
        entity.setStatus(AppointmentStatus.PENDING);
        entity.setPriceAmount(specialistService.getService().getPriceAmount());
        entity.setCurrency(specialistService.getService().getCurrency());

        return entity;
    }

    public AppointmentEntity applyPatch(
        AppointmentEntity entity,
        AppointmentPatchRequestDto request,
        UserEntity client,
        SpecialistServiceEntity specialistService
    ) {
        if (client != null) {
            entity.setClient(client);
        }
        if (specialistService != null) {
            entity.setSpecialist(specialistService.getSpecialist());
            entity.setSpecialistService(specialistService);
            entity.setPriceAmount(specialistService.getService().getPriceAmount());
            entity.setCurrency(specialistService.getService().getCurrency());
        }
        if (request.startTime() != null) {
            entity.setStartTime(request.startTime());
        }
        if (request.endTime() != null) {
            entity.setEndTime(request.endTime());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.paymentMethod() != null) {
            entity.setPaymentMethod(request.paymentMethod());
        }

        return entity;
    }

    public boolean hasAnyPatchField(AppointmentPatchRequestDto request) {
        return request.clientId() != null
            || request.specialistServiceId() != null
            || request.startTime() != null
            || request.endTime() != null
            || request.status() != null
            || request.paymentMethod() != null;
    }
}
