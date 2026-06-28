package com.sunfeax.citeria.service;

import java.time.Duration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.ForbiddenException;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.normalizer.AppointmentFieldNormalizer;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.validation.AppointmentValidator;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final UUID APPOINTMENT_ID = new UUID(0, 1L);
    private static final UUID CLIENT_ID = new UUID(0, 10L);
    private static final UUID SPECIALIST_ID = new UUID(0, 20L);
    private static final UUID SERVICE_ID = new UUID(0, 30L);

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentMapper appointmentMapper;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private AppointmentFieldNormalizer appointmentFieldNormalizer;
    @Mock
    private AppointmentValidator appointmentValidator;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
            appointmentRepository,
            appointmentMapper,
            serviceRepository,
            appointmentFieldNormalizer,
            appointmentValidator,
            currentUserProvider
        );
        ReflectionTestUtils.setField(appointmentService, "paymentWindowHours", 24L);
        ReflectionTestUtils.setField(appointmentService, "preAppointmentBufferHours", 6L);
    }

    @Test
    void listShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        AppointmentEntity entity = appointment(AppointmentStatus.PENDING, futureStart());
        AppointmentResponseDto dto = dto();

        when(currentUserProvider.getCurrentUser()).thenReturn(client());
        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(entity)));
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto);

        PageResponseDto<AppointmentResponseDto> result =
            appointmentService.list(null, null, null, null, pageable);

        assertEquals(1, result.totalElements());
        assertEquals(dto, result.content().getFirst());
    }

    @Test
    void getByIdShouldReturnDtoForParticipant() {
        AppointmentEntity entity = appointment(AppointmentStatus.PENDING, futureStart());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(client());
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        assertEquals(dto(), appointmentService.getById(APPOINTMENT_ID));
    }

    @Test
    void getByIdShouldThrowForNonParticipant() {
        AppointmentEntity entity = appointment(AppointmentStatus.PENDING, futureStart());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(user(new UUID(0, 999L), UserType.CLIENT));

        assertThrows(ForbiddenException.class, () -> appointmentService.getById(APPOINTMENT_ID));
    }

    @Test
    void createShouldSaveAppointment() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(SERVICE_ID, futureStart());
        UserEntity client = client();
        ServiceEntity service = service();
        AppointmentEntity entity = appointment(AppointmentStatus.PENDING, futureStart());

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(client);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(service));
        when(appointmentMapper.createEntity(request, client, service)).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        assertEquals(dto(), appointmentService.create(request));
        verify(appointmentValidator).validateCreate(request, client, service);
        verify(appointmentRepository).save(entity);
    }

    @Test
    void createShouldThrowWhenServiceNotFound() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(SERVICE_ID, futureStart());

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(client());
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void acceptShouldMoveToAwaitingPaymentAndSetDeadline() {
        AppointmentEntity entity = appointment(AppointmentStatus.PENDING, futureStart());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(specialist());
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentRepository.findBySpecialistIdAndStatusAndEndTimeGreaterThanAndStartTimeLessThan(
            any(), any(), any(), any())).thenReturn(List.of());
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        appointmentService.accept(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.AWAITING_PAYMENT, entity.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(entity.getPaymentDeadline());
        verify(appointmentRepository).save(entity);
    }

    @Test
    void acceptShouldRejectCompetingPendingRequests() {
        AppointmentEntity entity = appointment(AppointmentStatus.PENDING, futureStart());
        AppointmentEntity competitor = appointment(AppointmentStatus.PENDING, futureStart());
        competitor.setId(new UUID(0, 2L));

        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(specialist());
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentRepository.findBySpecialistIdAndStatusAndEndTimeGreaterThanAndStartTimeLessThan(
            any(), any(), any(), any())).thenReturn(List.of(competitor));
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        appointmentService.accept(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.REJECTED, competitor.getStatus());
        verify(appointmentRepository).saveAll(List.of(competitor));
    }

    @Test
    void acceptShouldThrowWhenNotPending() {
        AppointmentEntity entity = appointment(AppointmentStatus.CONFIRMED, futureStart());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(specialist());

        assertThrows(RequestValidationException.class, () -> appointmentService.accept(APPOINTMENT_ID));
    }

    @Test
    void acceptShouldThrowWhenCallerIsNotSpecialist() {
        AppointmentEntity entity = appointment(AppointmentStatus.PENDING, futureStart());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(client());

        assertThrows(ForbiddenException.class, () -> appointmentService.accept(APPOINTMENT_ID));
    }

    @Test
    void rejectShouldMoveToRejected() {
        AppointmentEntity entity = appointment(AppointmentStatus.PENDING, futureStart());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(specialist());
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        appointmentService.reject(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.REJECTED, entity.getStatus());
    }

    @Test
    void payShouldConfirmWhenWithinWindow() {
        AppointmentEntity entity = appointment(AppointmentStatus.AWAITING_PAYMENT, futureStart());
        entity.setPaymentDeadline(Instant.now().plus(Duration.ofHours(1)));
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(client());
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        appointmentService.pay(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.CONFIRMED, entity.getStatus());
    }

    @Test
    void payShouldExpireWhenWindowElapsed() {
        AppointmentEntity entity = appointment(AppointmentStatus.AWAITING_PAYMENT, futureStart());
        entity.setPaymentDeadline(Instant.now().minus(Duration.ofMinutes(1)));
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(client());

        assertThrows(RequestValidationException.class, () -> appointmentService.pay(APPOINTMENT_ID));
        assertEquals(AppointmentStatus.EXPIRED, entity.getStatus());
    }

    @Test
    void cancelShouldMoveToCancelled() {
        AppointmentEntity entity = appointment(AppointmentStatus.CONFIRMED, futureStart());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(client());
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        appointmentService.cancel(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.CANCELLED, entity.getStatus());
    }

    @Test
    void completeShouldMoveToCompleted() {
        AppointmentEntity entity = appointment(AppointmentStatus.CONFIRMED, futureStart());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(specialist());
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        appointmentService.complete(APPOINTMENT_ID);

        assertEquals(AppointmentStatus.COMPLETED, entity.getStatus());
    }

    @Test
    void deleteByIdShouldRequireAdmin() {
        when(currentUserProvider.requireAdmin()).thenThrow(new ForbiddenException("Administrator privileges are required"));

        assertThrows(ForbiddenException.class, () -> appointmentService.deleteById(APPOINTMENT_ID));
        verify(appointmentRepository, never()).delete(any(AppointmentEntity.class));
    }

    @Test
    void deleteByIdShouldDeleteForAdmin() {
        AppointmentEntity entity = appointment(AppointmentStatus.CANCELLED, futureStart());
        when(currentUserProvider.requireAdmin()).thenReturn(user(new UUID(0, 7L), UserType.SPECIALIST));
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(entity));
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto());

        appointmentService.deleteById(APPOINTMENT_ID);

        verify(appointmentRepository).delete(entity);
    }

    // helpers

    private static final Instant FIXED_START = Instant.parse("2026-09-01T10:00:00Z");

    private Instant futureStart() {
        return Instant.now().plus(Duration.ofDays(2));
    }

    private UserEntity user(UUID id, UserType type) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail(id + "@example.com");
        user.setType(type);
        user.setActive(true);
        return user;
    }

    private UserEntity client() {
        return user(CLIENT_ID, UserType.CLIENT);
    }

    private UserEntity specialist() {
        return user(SPECIALIST_ID, UserType.SPECIALIST);
    }

    private ServiceEntity service() {
        ServiceEntity service = new ServiceEntity();
        service.setId(SERVICE_ID);
        service.setSpecialist(specialist());
        service.setName("Consultation");
        service.setDurationMinutes(60);
        service.setPriceAmount(BigDecimal.valueOf(95));
        service.setCurrency("EUR");
        service.setActive(true);
        return service;
    }

    private AppointmentEntity appointment(AppointmentStatus status, Instant start) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(APPOINTMENT_ID);
        entity.setClient(client());
        entity.setSpecialist(specialist());
        entity.setService(service());
        entity.setStartTime(start);
        entity.setEndTime(start.plus(Duration.ofMinutes(60)));
        entity.setStatus(status);
        entity.setPriceAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        return entity;
    }

    private AppointmentResponseDto dto() {
        return new AppointmentResponseDto(
            APPOINTMENT_ID,
            CLIENT_ID,
            "First Last",
            CLIENT_ID + "@example.com",
            SERVICE_ID,
            "Consultation",
            SPECIALIST_ID,
            "First Last",
            FIXED_START,
            FIXED_START.plus(Duration.ofMinutes(60)),
            AppointmentStatus.PENDING,
            BigDecimal.valueOf(95),
            null
        );
    }
}
