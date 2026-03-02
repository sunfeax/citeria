package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.payment.PaymentResponseDto;
import com.sunfeax.citeria.entity.PaymentEntity;

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
}
