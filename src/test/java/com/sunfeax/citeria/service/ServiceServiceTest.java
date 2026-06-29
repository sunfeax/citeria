package com.sunfeax.citeria.service;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.mapper.ServiceMapper;
import com.sunfeax.citeria.normalizer.ServiceFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.validation.ServiceValidator;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    private static final UUID SPECIALIST_ID = new UUID(0, 10L);

    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private ServiceMapper serviceMapper;
    @Mock
    private ServiceFieldNormalizer serviceFieldNormalizer;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private ServiceValidator serviceValidator;

    private ServiceService serviceService;

    @BeforeEach
    void setUp() {
        serviceValidator = new ServiceValidator(serviceRepository, serviceMapper);
        serviceService = new ServiceService(
            serviceRepository,
            serviceMapper,
            serviceFieldNormalizer,
            serviceValidator,
            currentUserProvider
        );
    }

    @Test
    void getAllShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        ServiceResponseDto dto = serviceResponseDto(new UUID(0, 1L), "Consultation");

        when(serviceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        PageResponseDto<ServiceResponseDto> result = serviceService.list(null, null, null, null, null, pageable);

        assertEquals(1, result.totalElements());
        assertEquals(dto, result.content().getFirst());
    }

    @Test
    void getByIdShouldReturnServiceWhenExists() {
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        ServiceResponseDto dto = serviceResponseDto(new UUID(0, 1L), "Consultation");

        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.getById(new UUID(0, 1L));

        assertEquals(dto, result);
    }

    @Test
    void getByIdShouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(new UUID(0, 55L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.getById(new UUID(0, 55L)));
    }

    @Test
    void createShouldSaveServiceWhenRequestIsValid() {
        UserEntity specialist = specialist();
        ServicePostRequestDto request = new ServicePostRequestDto(
            "  Consultation  ", "desc", 60, BigDecimal.valueOf(95), "eur"
        );
        ServicePostRequestDto normalized = new ServicePostRequestDto(
            "Consultation", "desc", 60, BigDecimal.valueOf(95), "EUR"
        );
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        ServiceResponseDto dto = serviceResponseDto(new UUID(0, 1L), "Consultation");

        when(serviceFieldNormalizer.normalizePostRequest(request)).thenReturn(normalized);
        when(currentUserProvider.getCurrentUser()).thenReturn(specialist);
        when(serviceRepository.existsBySpecialistIdAndNameIgnoreCase(SPECIALIST_ID, "Consultation")).thenReturn(false);
        when(serviceMapper.createEntity(normalized, specialist)).thenReturn(entity);
        when(serviceRepository.save(entity)).thenReturn(entity);
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.create(request);

        assertEquals(dto, result);
        verify(serviceRepository).save(entity);
    }

    @Test
    void createShouldThrowWhenServiceNameExistsForSpecialist() {
        UserEntity specialist = specialist();
        ServicePostRequestDto request = new ServicePostRequestDto(
            "Consultation", "desc", 60, BigDecimal.valueOf(95), "EUR"
        );

        when(serviceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(specialist);
        when(serviceRepository.existsBySpecialistIdAndNameIgnoreCase(SPECIALIST_ID, "Consultation")).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> serviceService.create(request));
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void createShouldThrowWhenUserIsNotSpecialist() {
        UserEntity client = new UserEntity();
        client.setId(new UUID(0, 99L));
        client.setType(UserType.CLIENT);
        client.setActive(true);
        ServicePostRequestDto request = new ServicePostRequestDto(
            "Consultation", "desc", 60, BigDecimal.valueOf(95), "EUR"
        );

        when(serviceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(currentUserProvider.getCurrentUser()).thenReturn(client);

        assertThrows(RequestValidationException.class, () -> serviceService.create(request));
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void updateShouldThrowWhenServiceNotFound() {
        ServicePatchRequestDto request = new ServicePatchRequestDto("Consultation", null, null, null, null);

        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.update(new UUID(0, 1L), request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, null, null, null, null);

        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(serviceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(serviceMapper.hasAnyPatchField(request)).thenReturn(false);
        when(serviceRepository.existsBySpecialistIdAndNameIgnoreCaseAndIdNot(SPECIALIST_ID, "Consultation", new UUID(0, 1L))).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> serviceService.update(new UUID(0, 1L), request));
    }

    @Test
    void updateShouldThrowWhenDuplicateName() {
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        ServicePatchRequestDto request = new ServicePatchRequestDto("Therapy", null, null, null, null);

        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(serviceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(serviceMapper.hasAnyPatchField(request)).thenReturn(true);
        when(serviceRepository.existsBySpecialistIdAndNameIgnoreCaseAndIdNot(SPECIALIST_ID, "Therapy", new UUID(0, 1L))).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> serviceService.update(new UUID(0, 1L), request));
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        ServicePatchRequestDto request = new ServicePatchRequestDto(
            "Therapy",
            "new desc",
            90,
            BigDecimal.valueOf(120),
            "USD"
        );
        ServiceResponseDto dto = serviceResponseDto(new UUID(0, 1L), "Therapy");

        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(serviceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(serviceMapper.hasAnyPatchField(request)).thenReturn(true);
        when(serviceRepository.existsBySpecialistIdAndNameIgnoreCaseAndIdNot(SPECIALIST_ID, "Therapy", new UUID(0, 1L))).thenReturn(false);
        when(serviceMapper.applyPatch(entity, request)).thenReturn(entity);
        when(serviceRepository.save(entity)).thenReturn(entity);
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.update(new UUID(0, 1L), request);

        assertEquals(dto, result);
        verify(serviceRepository).save(entity);
    }

    @Test
    void deactivateShouldSetInactiveAndSave() {
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        ServiceResponseDto dto = serviceResponseDto(new UUID(0, 1L), "Consultation");

        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(serviceRepository.save(entity)).thenReturn(entity);
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.deactivateById(new UUID(0, 1L));

        assertEquals(dto, result);
        assertFalse(entity.isActive());
    }

    @Test
    void deactivateShouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.deactivateById(new UUID(0, 1L)));
    }

    @Test
    void hardDeleteShouldReturnDeletedService() {
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        ServiceResponseDto dto = serviceResponseDto(new UUID(0, 1L), "Consultation");

        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.hardDeleteById(new UUID(0, 1L));

        assertEquals(dto, result);
        verify(serviceRepository).delete(entity);
    }

    @Test
    void hardDeleteShouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.hardDeleteById(new UUID(0, 1L)));
    }

    @Test
    void restoreShouldSetActiveAndSave() {
        ServiceEntity entity = serviceEntity(new UUID(0, 1L), "Consultation");
        entity.setActive(false);
        ServiceResponseDto dto = serviceResponseDto(new UUID(0, 1L), "Consultation");

        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.of(entity));
        when(serviceRepository.save(entity)).thenReturn(entity);
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.restoreById(new UUID(0, 1L));

        assertEquals(dto, result);
        assertTrue(entity.isActive());
    }

    @Test
    void restoreShouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(new UUID(0, 1L))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.restoreById(new UUID(0, 1L)));
    }

    private UserEntity specialist() {
        UserEntity specialist = new UserEntity();
        specialist.setId(SPECIALIST_ID);
        specialist.setFirstName("Spec");
        specialist.setLastName("Ialist");
        specialist.setType(UserType.SPECIALIST);
        specialist.setActive(true);
        return specialist;
    }

    private ServiceEntity serviceEntity(UUID id, String name) {
        ServiceEntity entity = new ServiceEntity();
        entity.setId(id);
        entity.setSpecialist(specialist());
        entity.setName(name);
        entity.setDescription("desc");
        entity.setDurationMinutes(60);
        entity.setPriceAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        entity.setActive(true);
        return entity;
    }

    private ServiceResponseDto serviceResponseDto(UUID id, String name) {
        return new ServiceResponseDto(
            id,
            SPECIALIST_ID,
            "Spec Ialist",
            name,
            "desc",
            BigDecimal.valueOf(95),
            60,
            "EUR",
            true,
            Instant.now()
        );
    }
}
