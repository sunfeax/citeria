package com.sunfeax.citeria.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.payment.PaymentPatchRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentPostRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.PaymentEntity;
import com.sunfeax.citeria.enums.PaymentStatus;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.PaymentRepository;
import com.sunfeax.citeria.mapper.PaymentMapper;
import com.sunfeax.citeria.normalizer.PaymentFieldNormalizer;
import com.sunfeax.citeria.validation.PaymentValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.util.PageableUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final AppointmentRepository appointmentRepository;
    private final PaymentFieldNormalizer paymentFieldNormalizer;
    private final PaymentValidator paymentValidator;

    private static final Set<String> SORTABLE = Set.of("createdAt", "amount");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> list(PaymentStatus status, UUID appointmentId, Pageable pageable) {
        List<Specification<PaymentEntity>> specs = new ArrayList<>();
        if (status != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (appointmentId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("appointment").get("id"), appointmentId));
        }

        Pageable sanitized = PageableUtil.sanitizeSort(pageable, SORTABLE, DEFAULT_SORT);
        Page<PaymentResponseDto> page = paymentRepository.findAll(Specification.allOf(specs), sanitized)
            .map(paymentMapper::toResponseDto);

        return PageResponseDto.from(page);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getById(UUID id) {
        return paymentRepository.findById(id)
            .map(paymentMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + id + " not found"));
    }

    @Transactional
    public PaymentResponseDto create(PaymentPostRequestDto request) {
        PaymentPostRequestDto normalizedRequest = paymentFieldNormalizer.normalizePostRequest(request);

        AppointmentEntity appointment = findAppointmentOrThrow(normalizedRequest.appointmentId());
        paymentValidator.validateRegister(normalizedRequest, appointment);

        PaymentEntity entity = paymentMapper.createEntity(normalizedRequest, appointment);
        PaymentEntity saved = paymentRepository.save(entity);

        return paymentMapper.toResponseDto(saved);
    }

    @Transactional
    public PaymentResponseDto update(UUID id, PaymentPatchRequestDto request) {
        PaymentEntity entity = findPaymentOrThrow(id);

        PaymentPatchRequestDto normalizedRequest = paymentFieldNormalizer.normalizePatchRequest(request);

        AppointmentEntity targetAppointment = normalizedRequest.appointmentId() == null
            ? entity.getAppointment()
            : findAppointmentOrThrow(normalizedRequest.appointmentId());
        paymentValidator.validateUpdate(id, entity, normalizedRequest, targetAppointment);

        AppointmentEntity appointmentToApply = normalizedRequest.appointmentId() == null ? null : targetAppointment;

        paymentMapper.applyPatch(entity, normalizedRequest, appointmentToApply);
        PaymentEntity saved = paymentRepository.save(entity);

        return paymentMapper.toResponseDto(saved);
    }

    @Transactional
    public PaymentResponseDto deactivateById(UUID id) {
        PaymentEntity payment = findPaymentOrThrow(id);

        payment.setStatus(PaymentStatus.FAILED);
        PaymentEntity saved = paymentRepository.save(payment);

        return paymentMapper.toResponseDto(saved);
    }

    @Transactional
    public PaymentResponseDto deleteById(UUID id) {
        PaymentEntity payment = findPaymentOrThrow(id);

        PaymentResponseDto deletedPayment = paymentMapper.toResponseDto(payment);
        paymentRepository.delete(payment);

        return deletedPayment;
    }

    private PaymentEntity findPaymentOrThrow(UUID id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment with id " + id + " not found"));
    }

    private AppointmentEntity findAppointmentOrThrow(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment with id " + appointmentId + " not found"));
    }
}
