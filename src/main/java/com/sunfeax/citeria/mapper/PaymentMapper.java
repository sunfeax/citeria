package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.payment.PaymentPatchRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentPostRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.PaymentEntity;
import com.sunfeax.citeria.enums.PaymentStatus;

@Component
public class PaymentMapper {

    public PaymentResponseDto toResponseDto(PaymentEntity paymentEntity) {
        return new PaymentResponseDto(
            paymentEntity.getId(),
            paymentEntity.getAppointment().getId(),
            paymentEntity.getAmount(),
            paymentEntity.getCurrency(),
            paymentEntity.getStatus(),
            paymentEntity.getCreatedAt(),
            paymentEntity.getUpdatedAt()
        );
    }

    public PaymentEntity createEntity(PaymentPostRequestDto request, AppointmentEntity appointment) {
        PaymentEntity entity = new PaymentEntity();

        entity.setAppointment(appointment);
        entity.setAmount(appointment.getPriceAmount());
        entity.setCurrency(appointment.getCurrency());
        entity.setStatus(PaymentStatus.PENDING);

        return entity;
    }

    public PaymentEntity applyPatch(PaymentEntity entity, PaymentPatchRequestDto request, AppointmentEntity appointment) {
        if (appointment != null) {
            entity.setAppointment(appointment);
            entity.setAmount(appointment.getPriceAmount());
            entity.setCurrency(appointment.getCurrency());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        return entity;
    }

    public boolean hasAnyPatchField(PaymentPatchRequestDto request) {
        return request.appointmentId() != null || request.status() != null;
    }
}
