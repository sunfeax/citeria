package com.sunfeax.citeria.mapper;

import java.time.Duration;

import org.springframework.stereotype.Component;

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
            appointmentEntity.getPriceAmount(),
            appointmentEntity.getPaymentDeadline()
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
        entity.setEndTime(request.startTime().plus(Duration.ofMinutes(specialistService.getService().getDurationMinutes())));
        entity.setPaymentMethod(request.paymentMethod());
        entity.setStatus(AppointmentStatus.PENDING);
        entity.setPriceAmount(specialistService.getService().getPriceAmount());
        entity.setCurrency(specialistService.getService().getCurrency());

        return entity;
    }
}
