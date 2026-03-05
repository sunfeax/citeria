package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;

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
}
