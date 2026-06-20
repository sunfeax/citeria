package com.sunfeax.citeria.service;

import java.time.temporal.ChronoUnit;
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
import com.sunfeax.citeria.dto.common.PageResponseDto;
import org.springframework.data.jpa.domain.Specification;

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.PaymentMethod;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.ForbiddenException;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.exception.UnauthorizedException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.normalizer.AppointmentFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.validation.AppointmentValidator;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentMapper appointmentMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SpecialistServiceRepository specialistServiceRepository;
    @Mock
    private AppointmentFieldNormalizer appointmentFieldNormalizer;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private AppointmentValidator appointmentValidator;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentValidator = new AppointmentValidator(appointmentRepository, appointmentMapper);
        appointmentService = new AppointmentService(
            appointmentRepository,
            appointmentMapper,
            userRepository,
            specialistServiceRepository,
            appointmentFieldNormalizer,
            appointmentValidator,
            currentUserProvider
        );
    }

    @Test
    void getAllShouldReturnMappedPage() {
        stubAdminCurrentUser();
        Pageable pageable = PageRequest.of(0, 20);
        AppointmentEntity entity = appointmentEntity(new UUID(0, 1L));
        AppointmentResponseDto dto = appointmentDto(new UUID(0, 1L));

        when(appointmentRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto);

        PageResponseDto<AppointmentResponseDto> result = appointmentService.list(null, null, null, null, pageable);

        assertEquals(1, result.totalElements());
        assertEquals(dto, result.content().getFirst());
    }

    @Test
    void getByIdShouldThrowWhenAppointmentNotFound() {
        when(appointmentRepository.findById(new UUID(0, 99L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.getById(new UUID(0, 99L)));
    }

    @Test
    void getByIdShouldThrowForbiddenWhenCallerIsNotParticipant() {
        AppointmentEntity entity = appointmentEntity(new UUID(0, 1L));
        UserEntity stranger = clientUser(new UUID(0, 4242L));

        when(appointmentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(currentUserProvider.getCurrentUser()).thenReturn(stranger);
        when(currentUserProvider.isAdmin(stranger)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> appointmentService.getById(new UUID(0, 1L)));
    }

    @Test
    void createShouldSaveAppointmentWhenRequestIsValid() {
        Instant start = futureStart();
        Instant end = start.plus(Duration.ofMinutes(60));

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(new UUID(0, 100L), start, end, PaymentMethod.ONLINE);
        UserEntity client = clientUser(new UUID(0, 10L));
        SpecialistServiceEntity specialistService = specialistService(new UUID(0, 100L), true);
        AppointmentEntity entity = appointmentEntity(new UUID(0, 1L));
        AppointmentResponseDto dto = appointmentDto(new UUID(0, 1L));

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(client);
        when(specialistServiceRepository.findById(new UUID(0, 100L))).thenReturn(Optional.of(specialistService));
        when(
            appointmentRepository.existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNot(
                specialistService.getSpecialist().getId(),
                end,
                start,
                AppointmentStatus.CANCELLED
            )
        ).thenReturn(false);
        when(appointmentMapper.createEntity(request, client, specialistService)).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto);

        AppointmentResponseDto result = appointmentService.create(request);

        assertEquals(dto, result);
        verify(appointmentRepository).save(entity);
    }

    @Test
    void createShouldThrowWhenCurrentUserCannotBeResolved() {
        Instant start = futureStart();
        Instant end = start.plus(Duration.ofMinutes(60));

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(new UUID(0, 100L), start, end, PaymentMethod.ONLINE);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenThrow(new UnauthorizedException("Authentication is required"));

        assertThrows(UnauthorizedException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void createShouldThrowWhenSpecialistServiceNotFound() {
        Instant start = futureStart();
        Instant end = start.plus(Duration.ofMinutes(60));

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(new UUID(0, 100L), start, end, PaymentMethod.ONLINE);
        UserEntity client = clientUser(new UUID(0, 10L));

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(client);
        when(specialistServiceRepository.findById(new UUID(0, 100L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void createShouldThrowWhenClientHasWrongType() {
        Instant start = futureStart();
        Instant end = start.plus(Duration.ofMinutes(60));

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(new UUID(0, 100L), start, end, PaymentMethod.ONLINE);
        UserEntity specialistAsClient = specialistUser(new UUID(0, 10L));
        SpecialistServiceEntity specialistService = specialistService(new UUID(0, 100L), true);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(specialistAsClient);
        when(specialistServiceRepository.findById(new UUID(0, 100L))).thenReturn(Optional.of(specialistService));
        when(
            appointmentRepository.existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNot(
                specialistService.getSpecialist().getId(),
                end,
                start,
                AppointmentStatus.CANCELLED
            )
        ).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void createShouldThrowWhenTimeRangeIsInvalid() {
        Instant start = futureStart();
        Instant end = start.minus(Duration.ofMinutes(30));

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(new UUID(0, 100L), start, end, PaymentMethod.ONLINE);
        UserEntity client = clientUser(new UUID(0, 10L));
        SpecialistServiceEntity specialistService = specialistService(new UUID(0, 100L), true);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(client);
        when(specialistServiceRepository.findById(new UUID(0, 100L))).thenReturn(Optional.of(specialistService));
        when(
            appointmentRepository.existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNot(
                specialistService.getSpecialist().getId(),
                end,
                start,
                AppointmentStatus.CANCELLED
            )
        ).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void createShouldThrowWhenSpecialistIsAlreadyBooked() {
        Instant start = futureStart();
        Instant end = start.plus(Duration.ofMinutes(60));

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(new UUID(0, 100L), start, end, PaymentMethod.ONLINE);
        UserEntity client = clientUser(new UUID(0, 10L));
        SpecialistServiceEntity specialistService = specialistService(new UUID(0, 100L), true);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(client);
        when(specialistServiceRepository.findById(new UUID(0, 100L))).thenReturn(Optional.of(specialistService));
        when(
            appointmentRepository.existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNot(
                specialistService.getSpecialist().getId(),
                end,
                start,
                AppointmentStatus.CANCELLED
            )
        ).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void updateShouldThrowWhenAppointmentNotFound() {
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, null, null, null, null, null);

        when(appointmentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.update(new UUID(0, 1L), request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        stubAdminCurrentUser();
        AppointmentEntity entity = appointmentEntity(new UUID(0, 1L));
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, null, null, null, null, null);

        when(appointmentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(appointmentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentMapper.hasAnyPatchField(request)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> appointmentService.update(new UUID(0, 1L), request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void updateShouldThrowWhenTargetSpecialistServiceNotFound() {
        stubAdminCurrentUser();
        AppointmentEntity entity = appointmentEntity(new UUID(0, 1L));
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, new UUID(0, 200L), null, null, null, null);

        when(appointmentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(appointmentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(specialistServiceRepository.findById(new UUID(0, 200L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.update(new UUID(0, 1L), request));
    }

    @Test
    void updateShouldThrowWhenSpecialistIsAlreadyBooked() {
        stubAdminCurrentUser();
        AppointmentEntity entity = appointmentEntity(new UUID(0, 1L));
        Instant newStart = futureStart().plus(Duration.ofHours(1));
        Instant newEnd = newStart.plus(Duration.ofMinutes(60));
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, null, newStart, newEnd, null, null);

        when(appointmentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(appointmentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(
            appointmentRepository.existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNotAndIdNot(
                entity.getSpecialist().getId(),
                newEnd,
                newStart,
                AppointmentStatus.CANCELLED,
                new UUID(0, 1L)
            )
        ).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> appointmentService.update(new UUID(0, 1L), request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        stubAdminCurrentUser();
        AppointmentEntity entity = appointmentEntity(new UUID(0, 1L));
        Instant newStart = futureStart().plus(Duration.ofHours(1));
        Instant newEnd = newStart.plus(Duration.ofMinutes(60));
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(
            null,
            new UUID(0, 200L),
            newStart,
            newEnd,
            AppointmentStatus.CONFIRMED,
            PaymentMethod.ON_SITE
        );
        SpecialistServiceEntity newSpecialistService = specialistService(new UUID(0, 200L), true);
        AppointmentResponseDto dto = appointmentDto(new UUID(0, 1L));

        when(appointmentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(appointmentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(specialistServiceRepository.findById(new UUID(0, 200L))).thenReturn(Optional.of(newSpecialistService));
        when(appointmentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(
            appointmentRepository.existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNotAndIdNot(
                newSpecialistService.getSpecialist().getId(),
                newEnd,
                newStart,
                AppointmentStatus.CANCELLED,
                new UUID(0, 1L)
            )
        ).thenReturn(false);
        when(appointmentMapper.applyPatch(entity, request, null, newSpecialistService)).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto);

        AppointmentResponseDto result = appointmentService.update(new UUID(0, 1L), request);

        assertEquals(dto, result);
        verify(appointmentRepository).save(entity);
    }

    @Test
    void deleteShouldDeleteAndReturnDto() {
        stubAdminCurrentUser();
        AppointmentEntity entity = appointmentEntity(new UUID(0, 1L));
        AppointmentResponseDto dto = appointmentDto(new UUID(0, 1L));

        when(appointmentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto);

        AppointmentResponseDto result = appointmentService.deleteById(new UUID(0, 1L));

        assertEquals(dto, result);
        verify(appointmentRepository).delete(entity);
    }

    @Test
    void deleteShouldThrowWhenAppointmentNotFound() {
        when(appointmentRepository.findById(new UUID(0, 1L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.deleteById(new UUID(0, 1L)));
    }

    private UserEntity stubAdminCurrentUser() {
        UserEntity admin = new UserEntity();
        admin.setId(new UUID(0, 7L));
        admin.setEmail("admin@example.com");
        admin.setType(UserType.CLIENT);
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);
        when(currentUserProvider.isAdmin(admin)).thenReturn(true);
        return admin;
    }

    private Instant futureStart() {
        return Instant.now().plus(Duration.ofDays(1)).truncatedTo(ChronoUnit.MINUTES);
    }

    private UserEntity clientUser(UUID id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("Client");
        user.setLastName("User");
        user.setEmail("client@example.com");
        user.setType(UserType.CLIENT);
        user.setActive(true);
        return user;
    }

    private UserEntity specialistUser(UUID id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("Specialist");
        user.setLastName("User");
        user.setEmail("specialist@example.com");
        user.setType(UserType.SPECIALIST);
        user.setActive(true);
        return user;
    }

    private SpecialistServiceEntity specialistService(UUID id, boolean active) {
        BusinessEntity business = new BusinessEntity();
        business.setId(new UUID(0, 300L));
        business.setName("Alpha Studio");

        ServiceEntity service = new ServiceEntity();
        service.setId(new UUID(0, 400L));
        service.setName("Consultation");
        service.setPriceAmount(BigDecimal.valueOf(95));
        service.setCurrency("EUR");
        service.setActive(true);

        SpecialistServiceEntity entity = new SpecialistServiceEntity();
        entity.setId(id);
        entity.setBusiness(business);
        entity.setService(service);
        entity.setSpecialist(specialistUser(new UUID(0, 500L)));
        entity.setActive(active);
        return entity;
    }

    private AppointmentEntity appointmentEntity(UUID id) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(id);
        entity.setClient(clientUser(new UUID(0, 10L)));
        SpecialistServiceEntity specialistService = specialistService(new UUID(0, 100L), true);
        entity.setSpecialist(specialistService.getSpecialist());
        entity.setSpecialistService(specialistService);
        entity.setStartTime(futureStart());
        entity.setEndTime(futureStart().plus(Duration.ofMinutes(60)));
        entity.setStatus(AppointmentStatus.PENDING);
        entity.setPaymentMethod(PaymentMethod.ONLINE);
        entity.setPriceAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        return entity;
    }

    private AppointmentResponseDto appointmentDto(UUID id) {
        return new AppointmentResponseDto(
            id,
            new UUID(0, 10L),
            "Client User",
            "client@example.com",
            new UUID(0, 100L),
            new UUID(0, 500L),
            "Specialist User",
            new UUID(0, 400L),
            "Consultation",
            "Alpha Studio",
            futureStart(),
            futureStart().plus(Duration.ofMinutes(60)),
            AppointmentStatus.PENDING,
            PaymentMethod.ONLINE,
            BigDecimal.valueOf(95)
        );
    }
}
