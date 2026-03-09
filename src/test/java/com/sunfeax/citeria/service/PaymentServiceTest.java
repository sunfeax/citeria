package com.sunfeax.citeria.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sunfeax.citeria.dto.payment.PaymentPatchRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentPostRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.PaymentEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.PaymentMethod;
import com.sunfeax.citeria.enums.PaymentStatus;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.PaymentRepository;
import com.sunfeax.citeria.mapper.PaymentMapper;
import com.sunfeax.citeria.normalizer.PaymentFieldNormalizer;
import com.sunfeax.citeria.validation.PaymentValidator;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PaymentFieldNormalizer paymentFieldNormalizer;

    private PaymentValidator paymentValidator;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentValidator = new PaymentValidator(paymentRepository, paymentMapper);
        paymentService = new PaymentService(
            paymentRepository,
            paymentMapper,
            appointmentRepository,
            paymentFieldNormalizer,
            paymentValidator
        );
    }

    @Test
    void getAllShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentResponseDto dto = paymentDto(1L, 10L);

        when(paymentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        Page<PaymentResponseDto> result = paymentService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());
    }

    @Test
    void getByIdShouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getById(99L));
    }

    @Test
    void registerShouldSavePaymentWhenRequestIsValid() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(10L);
        AppointmentEntity appointment = appointmentEntity(10L, AppointmentStatus.CONFIRMED);
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentResponseDto dto = paymentDto(1L, 10L);

        when(paymentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(10L)).thenReturn(false);
        when(paymentMapper.createEntity(request, appointment)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PaymentResponseDto result = paymentService.create(request);

        assertEquals(dto, result);
        verify(paymentRepository).save(entity);
    }

    @Test
    void registerShouldThrowWhenAppointmentNotFound() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(10L);

        when(paymentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.create(request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void registerShouldThrowWhenPaymentAlreadyExistsForAppointment() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(10L);
        AppointmentEntity appointment = appointmentEntity(10L, AppointmentStatus.PENDING);

        when(paymentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(10L)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> paymentService.create(request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void registerShouldThrowWhenAppointmentIsCancelled() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(10L);
        AppointmentEntity appointment = appointmentEntity(10L, AppointmentStatus.CANCELLED);

        when(paymentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(10L)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> paymentService.create(request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void updateShouldThrowWhenPaymentNotFound() {
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(null, PaymentStatus.PAID);

        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(null, null);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(paymentMapper.hasAnyPatchField(request)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> paymentService.update(1L, request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void updateShouldThrowWhenTargetAppointmentNotFound() {
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(20L, null);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenPaymentForAppointmentAlreadyExists() {
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(20L, null);
        AppointmentEntity targetAppointment = appointmentEntity(20L, AppointmentStatus.CONFIRMED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(targetAppointment));
        when(paymentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(paymentRepository.existsByAppointmentIdAndIdNot(20L, 1L)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> paymentService.update(1L, request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void updateShouldThrowWhenTargetAppointmentCancelled() {
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(20L, PaymentStatus.PAID);
        AppointmentEntity targetAppointment = appointmentEntity(20L, AppointmentStatus.CANCELLED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(targetAppointment));
        when(paymentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(paymentRepository.existsByAppointmentIdAndIdNot(20L, 1L)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> paymentService.update(1L, request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(20L, PaymentStatus.PAID);
        AppointmentEntity targetAppointment = appointmentEntity(20L, AppointmentStatus.CONFIRMED);
        PaymentResponseDto dto = paymentDto(1L, 20L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(20L)).thenReturn(Optional.of(targetAppointment));
        when(paymentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(paymentRepository.existsByAppointmentIdAndIdNot(20L, 1L)).thenReturn(false);
        when(paymentMapper.applyPatch(entity, request, targetAppointment)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PaymentResponseDto result = paymentService.update(1L, request);

        assertEquals(dto, result);
        verify(paymentRepository).save(entity);
    }

    @Test
    void deactivateShouldSetFailedStatusAndSave() {
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentResponseDto dto = paymentDto(1L, 10L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PaymentResponseDto result = paymentService.deactivateById(1L);

        assertEquals(dto, result);
        assertEquals(PaymentStatus.FAILED, entity.getStatus());
    }

    @Test
    void deleteShouldDeleteAndReturnDto() {
        PaymentEntity entity = paymentEntity(1L, 10L);
        PaymentResponseDto dto = paymentDto(1L, 10L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PaymentResponseDto result = paymentService.deleteById(1L);

        assertEquals(dto, result);
        verify(paymentRepository).delete(entity);
    }

    @Test
    void deleteShouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.deleteById(1L));
    }

    private AppointmentEntity appointmentEntity(Long id, AppointmentStatus status) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(id);
        entity.setStatus(status);
        entity.setPaymentMethod(PaymentMethod.ONLINE);
        entity.setPriceAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        entity.setStartTime(LocalDateTime.now().plusDays(1));
        entity.setEndTime(LocalDateTime.now().plusDays(1).plusMinutes(60));
        return entity;
    }

    private PaymentEntity paymentEntity(Long id, Long appointmentId) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(id);
        entity.setAppointment(appointmentEntity(appointmentId, AppointmentStatus.PENDING));
        entity.setAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        entity.setStatus(PaymentStatus.PENDING);
        return entity;
    }

    private PaymentResponseDto paymentDto(Long id, Long appointmentId) {
        return new PaymentResponseDto(
            id,
            appointmentId,
            BigDecimal.valueOf(95),
            "EUR",
            PaymentStatus.PENDING,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}
