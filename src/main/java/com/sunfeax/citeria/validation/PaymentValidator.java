package com.sunfeax.citeria.validation;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.payment.PaymentPatchRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentPostRequestDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.PaymentEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.mapper.PaymentMapper;
import com.sunfeax.citeria.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public void validateRegister(PaymentPostRequestDto request, AppointmentEntity appointment) {
        new ValidationResult()
            .addErrorIf(
                paymentRepository.existsByAppointmentId(request.appointmentId()),
                "appointmentId",
                "Payment for appointment id " + request.appointmentId() + " already exists"
            )
            .addErrorIf(
                appointment.getStatus() == AppointmentStatus.CANCELLED,
                "appointmentId",
                "Cannot create payment for cancelled appointment"
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(
        Long id,
        PaymentEntity existingEntity,
        PaymentPatchRequestDto request,
        AppointmentEntity targetAppointment
    ) {
        Long targetAppointmentId = request.appointmentId() != null
            ? request.appointmentId()
            : existingEntity.getAppointment().getId();

        new ValidationResult()
            .addErrorIf(!paymentMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                request.appointmentId() != null
                    && paymentRepository.existsByAppointmentIdAndIdNot(targetAppointmentId, id),
                "appointmentId",
                "Payment for appointment id " + targetAppointmentId + " already exists"
            )
            .addErrorIf(
                request.appointmentId() != null
                    && targetAppointment.getStatus() == AppointmentStatus.CANCELLED,
                "appointmentId",
                "Cannot assign payment to cancelled appointment"
            )
            .throwIfHasErrors();
    }
}
