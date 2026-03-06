package com.sunfeax.citeria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.normalizer.AppointmentFieldNormalizer;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
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
            appointmentValidator
        );
    }

    @Test
    void getAllShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        AppointmentEntity entity = appointmentEntity(1L);
        AppointmentResponseDto dto = appointmentDto(1L);

        when(appointmentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto);

        Page<AppointmentResponseDto> result = appointmentService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());
    }

    @Test
    void getByIdShouldThrowWhenAppointmentNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.getById(99L));
    }

    @Test
    void createShouldSaveAppointmentWhenRequestIsValid() {
        LocalDateTime start = futureStart();
        LocalDateTime end = start.plusMinutes(60);

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(10L, 100L, start, end, PaymentMethod.ONLINE);
        UserEntity client = clientUser(10L);
        SpecialistServiceEntity specialistService = specialistService(100L, true);
        AppointmentEntity entity = appointmentEntity(1L);
        AppointmentResponseDto dto = appointmentDto(1L);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.findById(10L)).thenReturn(Optional.of(client));
        when(specialistServiceRepository.findById(100L)).thenReturn(Optional.of(specialistService));
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
    void createShouldThrowWhenClientNotFound() {
        LocalDateTime start = futureStart();
        LocalDateTime end = start.plusMinutes(60);

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(10L, 100L, start, end, PaymentMethod.ONLINE);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void createShouldThrowWhenSpecialistServiceNotFound() {
        LocalDateTime start = futureStart();
        LocalDateTime end = start.plusMinutes(60);

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(10L, 100L, start, end, PaymentMethod.ONLINE);
        UserEntity client = clientUser(10L);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.findById(10L)).thenReturn(Optional.of(client));
        when(specialistServiceRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.create(request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void createShouldThrowWhenClientHasWrongType() {
        LocalDateTime start = futureStart();
        LocalDateTime end = start.plusMinutes(60);

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(10L, 100L, start, end, PaymentMethod.ONLINE);
        UserEntity specialistAsClient = specialistUser(10L);
        SpecialistServiceEntity specialistService = specialistService(100L, true);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.findById(10L)).thenReturn(Optional.of(specialistAsClient));
        when(specialistServiceRepository.findById(100L)).thenReturn(Optional.of(specialistService));
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
        LocalDateTime start = futureStart();
        LocalDateTime end = start.minusMinutes(30);

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(10L, 100L, start, end, PaymentMethod.ONLINE);
        UserEntity client = clientUser(10L);
        SpecialistServiceEntity specialistService = specialistService(100L, true);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.findById(10L)).thenReturn(Optional.of(client));
        when(specialistServiceRepository.findById(100L)).thenReturn(Optional.of(specialistService));
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
        LocalDateTime start = futureStart();
        LocalDateTime end = start.plusMinutes(60);

        AppointmentPostRequestDto request = new AppointmentPostRequestDto(10L, 100L, start, end, PaymentMethod.ONLINE);
        UserEntity client = clientUser(10L);
        SpecialistServiceEntity specialistService = specialistService(100L, true);

        when(appointmentFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.findById(10L)).thenReturn(Optional.of(client));
        when(specialistServiceRepository.findById(100L)).thenReturn(Optional.of(specialistService));
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

        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        AppointmentEntity entity = appointmentEntity(1L);
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, null, null, null, null, null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appointmentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentMapper.hasAnyPatchField(request)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> appointmentService.update(1L, request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void updateShouldThrowWhenTargetSpecialistServiceNotFound() {
        AppointmentEntity entity = appointmentEntity(1L);
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, 200L, null, null, null, null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appointmentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(specialistServiceRepository.findById(200L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenSpecialistIsAlreadyBooked() {
        AppointmentEntity entity = appointmentEntity(1L);
        LocalDateTime newStart = futureStart().plusHours(1);
        LocalDateTime newEnd = newStart.plusMinutes(60);
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, null, newStart, newEnd, null, null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appointmentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(appointmentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(
            appointmentRepository.existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNotAndIdNot(
                entity.getSpecialist().getId(),
                newEnd,
                newStart,
                AppointmentStatus.CANCELLED,
                1L
            )
        ).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> appointmentService.update(1L, request));
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        AppointmentEntity entity = appointmentEntity(1L);
        LocalDateTime newStart = futureStart().plusHours(1);
        LocalDateTime newEnd = newStart.plusMinutes(60);
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(
            null,
            200L,
            newStart,
            newEnd,
            AppointmentStatus.CONFIRMED,
            PaymentMethod.ON_SITE
        );
        SpecialistServiceEntity newSpecialistService = specialistService(200L, true);
        AppointmentResponseDto dto = appointmentDto(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appointmentFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(specialistServiceRepository.findById(200L)).thenReturn(Optional.of(newSpecialistService));
        when(appointmentMapper.hasAnyPatchField(request)).thenReturn(true);
        when(
            appointmentRepository.existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNotAndIdNot(
                newSpecialistService.getSpecialist().getId(),
                newEnd,
                newStart,
                AppointmentStatus.CANCELLED,
                1L
            )
        ).thenReturn(false);
        when(appointmentMapper.applyPatch(entity, request, null, newSpecialistService)).thenReturn(entity);
        when(appointmentRepository.save(entity)).thenReturn(entity);
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto);

        AppointmentResponseDto result = appointmentService.update(1L, request);

        assertEquals(dto, result);
        verify(appointmentRepository).save(entity);
    }

    @Test
    void deleteShouldDeleteAndReturnDto() {
        AppointmentEntity entity = appointmentEntity(1L);
        AppointmentResponseDto dto = appointmentDto(1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appointmentMapper.toResponseDto(entity)).thenReturn(dto);

        AppointmentResponseDto result = appointmentService.deleteById(1L);

        assertEquals(dto, result);
        verify(appointmentRepository).delete(entity);
    }

    @Test
    void deleteShouldThrowWhenAppointmentNotFound() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.deleteById(1L));
    }

    private LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
    }

    private UserEntity clientUser(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("Client");
        user.setLastName("User");
        user.setEmail("client@example.com");
        user.setType(UserType.CLIENT);
        user.setActive(true);
        return user;
    }

    private UserEntity specialistUser(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("Specialist");
        user.setLastName("User");
        user.setEmail("specialist@example.com");
        user.setType(UserType.SPECIALIST);
        user.setActive(true);
        return user;
    }

    private SpecialistServiceEntity specialistService(Long id, boolean active) {
        BusinessEntity business = new BusinessEntity();
        business.setId(300L);
        business.setName("Alpha Studio");

        ServiceEntity service = new ServiceEntity();
        service.setId(400L);
        service.setName("Consultation");
        service.setPriceAmount(BigDecimal.valueOf(95));
        service.setCurrency("EUR");
        service.setActive(true);

        SpecialistServiceEntity entity = new SpecialistServiceEntity();
        entity.setId(id);
        entity.setBusiness(business);
        entity.setService(service);
        entity.setSpecialist(specialistUser(500L));
        entity.setActive(active);
        return entity;
    }

    private AppointmentEntity appointmentEntity(Long id) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(id);
        entity.setClient(clientUser(10L));
        SpecialistServiceEntity specialistService = specialistService(100L, true);
        entity.setSpecialist(specialistService.getSpecialist());
        entity.setSpecialistService(specialistService);
        entity.setStartTime(futureStart());
        entity.setEndTime(futureStart().plusMinutes(60));
        entity.setStatus(AppointmentStatus.PENDING);
        entity.setPaymentMethod(PaymentMethod.ONLINE);
        entity.setPriceAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        return entity;
    }

    private AppointmentResponseDto appointmentDto(Long id) {
        return new AppointmentResponseDto(
            id,
            10L,
            "Client User",
            "client@example.com",
            100L,
            500L,
            "Specialist User",
            400L,
            "Consultation",
            "Alpha Studio",
            futureStart(),
            futureStart().plusMinutes(60),
            AppointmentStatus.PENDING,
            PaymentMethod.ONLINE,
            BigDecimal.valueOf(95)
        );
    }
}
