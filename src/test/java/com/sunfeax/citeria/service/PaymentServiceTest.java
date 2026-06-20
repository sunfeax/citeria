package com.sunfeax.citeria.service;

import java.time.Duration;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import org.springframework.data.jpa.domain.Specification;

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
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentResponseDto dto = paymentDto(new UUID(0, 1L), new UUID(0, 10L));

        when(paymentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PageResponseDto<PaymentResponseDto> result = paymentService.list(null, null, pageable);

        assertEquals(1, result.totalElements());
        assertEquals(dto, result.content().getFirst());
    }

    @Test
    void getByIdShouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById(new UUID(0, 99L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getById(new UUID(0, 99L)));
    }

    @Test
    void registerShouldSavePaymentWhenRequestIsValid() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(new UUID(0, 10L));
        AppointmentEntity appointment = appointmentEntity(new UUID(0, 10L), AppointmentStatus.CONFIRMED);
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentResponseDto dto = paymentDto(new UUID(0, 1L), new UUID(0, 10L));

        when(paymentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(new UUID(0, 10L))).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(new UUID(0, 10L))).thenReturn(false);
        when(paymentMapper.createEntity(request, appointment)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PaymentResponseDto result = paymentService.create(request);

        assertEquals(dto, result);
        verify(paymentRepository).save(entity);
    }

    @Test
    void registerShouldThrowWhenAppointmentNotFound() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(new UUID(0, 10L));

        when(paymentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(new UUID(0, 10L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.create(request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void registerShouldThrowWhenPaymentAlreadyExistsForAppointment() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(new UUID(0, 10L));
        AppointmentEntity appointment = appointmentEntity(new UUID(0, 10L), AppointmentStatus.PENDING);

        when(paymentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(new UUID(0, 10L))).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(new UUID(0, 10L))).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> paymentService.create(request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void registerShouldThrowWhenAppointmentIsCancelled() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(new UUID(0, 10L));
        AppointmentEntity appointment = appointmentEntity(new UUID(0, 10L), AppointmentStatus.CANCELLED);

        when(paymentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(new UUID(0, 10L))).thenReturn(Optional.of(appointment));
        when(paymentRepository.existsByAppointmentId(new UUID(0, 10L))).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> paymentService.create(request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void updateShouldThrowWhenPaymentNotFound() {
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(null, PaymentStatus.PAID);

        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.update(new UUID(0, 1L), request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(null, null);

        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(paymentMapper.hasAnyPatchField(request)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> paymentService.update(new UUID(0, 1L), request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void updateShouldThrowWhenTargetAppointmentNotFound() {
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(new UUID(0, 20L), null);

        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(new UUID(0, 20L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.update(new UUID(0, 1L), request));
    }

    @Test
    void updateShouldThrowWhenPaymentForAppointmentAlreadyExists() {
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(new UUID(0, 20L), null);
        AppointmentEntity targetAppointment = appointmentEntity(new UUID(0, 20L), AppointmentStatus.CONFIRMED);

        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(new UUID(0, 20L))).thenReturn(Optional.of(targetAppointment));
        when(paymentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(paymentRepository.existsByAppointmentIdAndIdNot(new UUID(0, 20L), new UUID(0, 1L))).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> paymentService.update(new UUID(0, 1L), request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void updateShouldThrowWhenTargetAppointmentCancelled() {
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(new UUID(0, 20L), PaymentStatus.PAID);
        AppointmentEntity targetAppointment = appointmentEntity(new UUID(0, 20L), AppointmentStatus.CANCELLED);

        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(new UUID(0, 20L))).thenReturn(Optional.of(targetAppointment));
        when(paymentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(paymentRepository.existsByAppointmentIdAndIdNot(new UUID(0, 20L), new UUID(0, 1L))).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> paymentService.update(new UUID(0, 1L), request));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(new UUID(0, 20L), PaymentStatus.PAID);
        AppointmentEntity targetAppointment = appointmentEntity(new UUID(0, 20L), AppointmentStatus.CONFIRMED);
        PaymentResponseDto dto = paymentDto(new UUID(0, 1L), new UUID(0, 20L));

        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(paymentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentRepository.findById(new UUID(0, 20L))).thenReturn(Optional.of(targetAppointment));
        when(paymentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(paymentRepository.existsByAppointmentIdAndIdNot(new UUID(0, 20L), new UUID(0, 1L))).thenReturn(false);
        when(paymentMapper.applyPatch(entity, request, targetAppointment)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PaymentResponseDto result = paymentService.update(new UUID(0, 1L), request);

        assertEquals(dto, result);
        verify(paymentRepository).save(entity);
    }

    @Test
    void deactivateShouldSetFailedStatusAndSave() {
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentResponseDto dto = paymentDto(new UUID(0, 1L), new UUID(0, 10L));

        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(paymentRepository.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PaymentResponseDto result = paymentService.deactivateById(new UUID(0, 1L));

        assertEquals(dto, result);
        assertEquals(PaymentStatus.FAILED, entity.getStatus());
    }

    @Test
    void deleteShouldDeleteAndReturnDto() {
        PaymentEntity entity = paymentEntity(new UUID(0, 1L), new UUID(0, 10L));
        PaymentResponseDto dto = paymentDto(new UUID(0, 1L), new UUID(0, 10L));

        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(paymentMapper.toResponseDto(entity)).thenReturn(dto);

        PaymentResponseDto result = paymentService.deleteById(new UUID(0, 1L));

        assertEquals(dto, result);
        verify(paymentRepository).delete(entity);
    }

    @Test
    void deleteShouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.deleteById(new UUID(0, 1L)));
    }

    private AppointmentEntity appointmentEntity(UUID id, AppointmentStatus status) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(id);
        entity.setStatus(status);
        entity.setPaymentMethod(PaymentMethod.ONLINE);
        entity.setPriceAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        entity.setStartTime(Instant.now().plus(Duration.ofDays(1)));
        entity.setEndTime(Instant.now().plus(Duration.ofDays(1)).plus(Duration.ofMinutes(60)));
        return entity;
    }

    private PaymentEntity paymentEntity(UUID id, UUID appointmentId) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(id);
        entity.setAppointment(appointmentEntity(appointmentId, AppointmentStatus.PENDING));
        entity.setAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        entity.setStatus(PaymentStatus.PENDING);
        return entity;
    }

    private PaymentResponseDto paymentDto(UUID id, UUID appointmentId) {
        return new PaymentResponseDto(
            id,
            appointmentId,
            BigDecimal.valueOf(95),
            "EUR",
            PaymentStatus.PENDING,
            Instant.now(),
            Instant.now()
        );
    }
}
