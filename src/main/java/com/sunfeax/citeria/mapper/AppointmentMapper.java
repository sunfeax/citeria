package com.sunfeax.citeria.mapper;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;

@Component
public class AppointmentMapper {

    public AppointmentResponseDto toResponseDto(AppointmentEntity appointmentEntity) {
        ServiceEntity service = appointmentEntity.getService();
        UserEntity client = appointmentEntity.getClient();
        UserEntity specialist = appointmentEntity.getSpecialist();

        return new AppointmentResponseDto(
            appointmentEntity.getId(),
            client.getId(),
            client.getFirstName() + " " + client.getLastName(),
            client.getEmail(),
            service.getId(),
            service.getName(),
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
        ServiceEntity service
    ) {
        AppointmentEntity entity = new AppointmentEntity();

        entity.setClient(client);
        entity.setSpecialist(service.getSpecialist());
        entity.setService(service);
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.startTime().plus(Duration.ofMinutes(service.getDurationMinutes())));
        entity.setStatus(AppointmentStatus.PENDING);
        entity.setPriceAmount(service.getPriceAmount());
        entity.setCurrency(service.getCurrency());

        return entity;
    }
}
