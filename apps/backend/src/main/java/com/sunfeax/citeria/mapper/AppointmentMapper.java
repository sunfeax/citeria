package com.sunfeax.citeria.mapper;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.OfferingEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;

@Component
public class AppointmentMapper {

    public AppointmentResponseDto toResponseDto(AppointmentEntity appointmentEntity) {
        OfferingEntity offering = appointmentEntity.getOffering();
        UserEntity client = appointmentEntity.getClient();
        UserEntity specialist = appointmentEntity.getSpecialist();

        return new AppointmentResponseDto(
            appointmentEntity.getId(),
            client.getId(),
            client.getFirstName() + " " + client.getLastName(),
            client.getEmail(),
            offering.getId(),
            offering.getName(),
            specialist.getId(),
            specialist.getFirstName() + " " + specialist.getLastName(),
            appointmentEntity.getStartTime(),
            appointmentEntity.getEndTime(),
            appointmentEntity.getStatus(),
            appointmentEntity.getPriceAmount(),
            appointmentEntity.getPaymentDeadline()
        );
    }

    public AppointmentEntity createEntity(
        AppointmentPostRequestDto request,
        UserEntity client,
        OfferingEntity offering
    ) {
        AppointmentEntity entity = new AppointmentEntity();

        entity.setClient(client);
        entity.setSpecialist(offering.getSpecialist());
        entity.setOffering(offering);
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.startTime().plus(Duration.ofMinutes(offering.getDurationMinutes())));
        entity.setStatus(AppointmentStatus.PENDING);
        entity.setPriceAmount(offering.getPriceAmount());
        entity.setCurrency(offering.getCurrency());

        return entity;
    }
}
