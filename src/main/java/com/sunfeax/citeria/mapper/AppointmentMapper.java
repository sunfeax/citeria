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
            appointmentEntity.getOffering().getId(),
            appointmentEntity.getOffering().getSpecialist().getId(),
            appointmentEntity.getOffering().getSpecialist().getFirstName() + " "
                + appointmentEntity.getOffering().getSpecialist().getLastName(),
            appointmentEntity.getOffering().getService().getId(),
            appointmentEntity.getOffering().getService().getName(),
            appointmentEntity.getOffering().getBusiness().getName(),
            appointmentEntity.getStartTime(),
            appointmentEntity.getEndTime(),
            appointmentEntity.getStatus(),
            appointmentEntity.getPaymentMethod(),
            appointmentEntity.getPriceAmount()
        );
    }
}
