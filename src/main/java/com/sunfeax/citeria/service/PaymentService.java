package com.sunfeax.citeria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.payment.PaymentResponseDto;
import com.sunfeax.citeria.mapper.PaymentMapper;
import com.sunfeax.citeria.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getAll() {
        return paymentRepository.findAll()
            .stream()
            .map(paymentMapper::toResponseDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponseDto> getById(Long id) {
        return paymentRepository.findById(id)
            .map(paymentMapper::toResponseDto);
    }
}
