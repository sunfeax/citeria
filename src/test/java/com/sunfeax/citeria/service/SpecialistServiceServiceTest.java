package com.sunfeax.citeria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.mapper.SpecialistServiceMapper;
import com.sunfeax.citeria.normalizer.SpecialistServiceFieldNormalizer;
import com.sunfeax.citeria.validation.SpecialistServiceValidator;

@ExtendWith(MockitoExtension.class)
class SpecialistServiceServiceTest {

    @Mock
    private SpecialistServiceRepository specialistServiceRepository;
    @Mock
    private SpecialistServiceMapper specialistServiceMapper;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private SpecialistServiceFieldNormalizer specialistServiceFieldNormalizer;

    private SpecialistServiceValidator specialistServiceValidator;

    private SpecialistServiceService specialistServiceService;

    @BeforeEach
    void setUp() {
        specialistServiceValidator = new SpecialistServiceValidator(specialistServiceRepository, specialistServiceMapper);
        specialistServiceService = new SpecialistServiceService(
            specialistServiceRepository,
            specialistServiceMapper,
            businessRepository,
            userRepository,
            serviceRepository,
            specialistServiceFieldNormalizer,
            specialistServiceValidator
        );
    }

    @Test
    void getAllShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        SpecialistServiceEntity entity = specialistServiceEntity(1L, 10L, 20L, 30L);
        SpecialistServiceResponseDto dto = specialistServiceDto(1L, 10L, 20L, 30L);

        when(specialistServiceRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(specialistServiceMapper.toResponseDto(entity)).thenReturn(dto);

        Page<SpecialistServiceResponseDto> result = specialistServiceService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());
    }

    @Test
    void getByIdShouldThrowWhenNotFound() {
        when(specialistServiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> specialistServiceService.getById(99L));
    }

    @Test
    void registerShouldSaveWhenRequestIsValid() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(10L, 20L, 30L);
        BusinessEntity business = businessEntity(10L, true);
        UserEntity specialist = specialistUser(20L, true);
        ServiceEntity service = serviceEntity(30L, 10L, true);
        SpecialistServiceEntity entity = specialistServiceEntity(1L, 10L, 20L, 30L);
        SpecialistServiceResponseDto dto = specialistServiceDto(1L, 10L, 20L, 30L);

        when(specialistServiceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(userRepository.findById(20L)).thenReturn(Optional.of(specialist));
        when(serviceRepository.findById(30L)).thenReturn(Optional.of(service));
        when(specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceId(10L, 20L, 30L)).thenReturn(false);
        when(specialistServiceMapper.createEntity(request, business, specialist, service)).thenReturn(entity);
        when(specialistServiceRepository.save(entity)).thenReturn(entity);
        when(specialistServiceMapper.toResponseDto(entity)).thenReturn(dto);

        SpecialistServiceResponseDto result = specialistServiceService.register(request);

        assertEquals(dto, result);
        verify(specialistServiceRepository).save(entity);
    }

    @Test
    void registerShouldThrowWhenDuplicateExists() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(10L, 20L, 30L);
        BusinessEntity business = businessEntity(10L, true);
        UserEntity specialist = specialistUser(20L, true);
        ServiceEntity service = serviceEntity(30L, 10L, true);

        when(specialistServiceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(userRepository.findById(20L)).thenReturn(Optional.of(specialist));
        when(serviceRepository.findById(30L)).thenReturn(Optional.of(service));
        when(specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceId(10L, 20L, 30L)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> specialistServiceService.register(request));
        verify(specialistServiceRepository, never()).save(any(SpecialistServiceEntity.class));
    }

    @Test
    void registerShouldThrowWhenBusinessNotFound() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(10L, 20L, 30L);

        when(specialistServiceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(businessRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> specialistServiceService.register(request));
    }

    @Test
    void registerShouldThrowWhenSpecialistWrongType() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(10L, 20L, 30L);
        BusinessEntity business = businessEntity(10L, true);
        UserEntity client = clientUser(20L, true);
        ServiceEntity service = serviceEntity(30L, 10L, true);

        when(specialistServiceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(userRepository.findById(20L)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(30L)).thenReturn(Optional.of(service));
        when(specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceId(10L, 20L, 30L)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> specialistServiceService.register(request));
        verify(specialistServiceRepository, never()).save(any(SpecialistServiceEntity.class));
    }

    @Test
    void registerShouldThrowWhenServiceBelongsToAnotherBusiness() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(10L, 20L, 30L);
        BusinessEntity business = businessEntity(10L, true);
        UserEntity specialist = specialistUser(20L, true);
        ServiceEntity service = serviceEntity(30L, 999L, true);

        when(specialistServiceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(userRepository.findById(20L)).thenReturn(Optional.of(specialist));
        when(serviceRepository.findById(30L)).thenReturn(Optional.of(service));
        when(specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceId(10L, 20L, 30L)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> specialistServiceService.register(request));
        verify(specialistServiceRepository, never()).save(any(SpecialistServiceEntity.class));
    }

    @Test
    void updateShouldThrowWhenNotFound() {
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(null, null, null);

        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> specialistServiceService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        SpecialistServiceEntity entity = specialistServiceEntity(1L, 10L, 20L, 30L);
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(null, null, null);

        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(specialistServiceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(specialistServiceMapper.hasAnyPatchField(request)).thenReturn(false);
        when(specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceIdAndIdNot(10L, 20L, 30L, 1L))
            .thenReturn(false);

        assertThrows(RequestValidationException.class, () -> specialistServiceService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenDuplicateExists() {
        SpecialistServiceEntity entity = specialistServiceEntity(1L, 10L, 20L, 30L);
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(11L, 21L, 31L);
        BusinessEntity business = businessEntity(11L, true);
        UserEntity specialist = specialistUser(21L, true);
        ServiceEntity service = serviceEntity(31L, 11L, true);

        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(specialistServiceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(businessRepository.findById(11L)).thenReturn(Optional.of(business));
        when(userRepository.findById(21L)).thenReturn(Optional.of(specialist));
        when(serviceRepository.findById(31L)).thenReturn(Optional.of(service));
        when(specialistServiceMapper.hasAnyPatchField(request)).thenReturn(true);
        when(specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceIdAndIdNot(11L, 21L, 31L, 1L))
            .thenReturn(true);

        assertThrows(RequestValidationException.class, () -> specialistServiceService.update(1L, request));
        verify(specialistServiceRepository, never()).save(any(SpecialistServiceEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        SpecialistServiceEntity entity = specialistServiceEntity(1L, 10L, 20L, 30L);
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(11L, 21L, 31L);
        BusinessEntity business = businessEntity(11L, true);
        UserEntity specialist = specialistUser(21L, true);
        ServiceEntity service = serviceEntity(31L, 11L, true);
        SpecialistServiceResponseDto dto = specialistServiceDto(1L, 11L, 21L, 31L);

        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(specialistServiceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(businessRepository.findById(11L)).thenReturn(Optional.of(business));
        when(userRepository.findById(21L)).thenReturn(Optional.of(specialist));
        when(serviceRepository.findById(31L)).thenReturn(Optional.of(service));
        when(specialistServiceMapper.hasAnyPatchField(request)).thenReturn(true);
        when(specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceIdAndIdNot(11L, 21L, 31L, 1L))
            .thenReturn(false);
        when(specialistServiceMapper.applyPatch(entity, request, business, specialist, service)).thenReturn(entity);
        when(specialistServiceRepository.save(entity)).thenReturn(entity);
        when(specialistServiceMapper.toResponseDto(entity)).thenReturn(dto);

        SpecialistServiceResponseDto result = specialistServiceService.update(1L, request);

        assertEquals(dto, result);
        verify(specialistServiceRepository).save(entity);
    }

    @Test
    void deactivateShouldSetInactiveAndSave() {
        SpecialistServiceEntity entity = specialistServiceEntity(1L, 10L, 20L, 30L);
        SpecialistServiceResponseDto dto = specialistServiceDto(1L, 10L, 20L, 30L);

        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(specialistServiceRepository.save(entity)).thenReturn(entity);
        when(specialistServiceMapper.toResponseDto(entity)).thenReturn(dto);

        SpecialistServiceResponseDto result = specialistServiceService.deactivateById(1L);

        assertEquals(dto, result);
        assertFalse(entity.isActive());
    }

    @Test
    void deactivateShouldThrowWhenNotFound() {
        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> specialistServiceService.deactivateById(1L));
    }

    @Test
    void hardDeleteShouldDeleteAndReturnDto() {
        SpecialistServiceEntity entity = specialistServiceEntity(1L, 10L, 20L, 30L);
        SpecialistServiceResponseDto dto = specialistServiceDto(1L, 10L, 20L, 30L);

        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(specialistServiceMapper.toResponseDto(entity)).thenReturn(dto);

        SpecialistServiceResponseDto result = specialistServiceService.hardDeleteById(1L);

        assertEquals(dto, result);
        verify(specialistServiceRepository).delete(entity);
    }

    @Test
    void hardDeleteShouldThrowWhenNotFound() {
        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> specialistServiceService.hardDeleteById(1L));
    }

    @Test
    void restoreShouldSetActiveAndSave() {
        SpecialistServiceEntity entity = specialistServiceEntity(1L, 10L, 20L, 30L);
        entity.setActive(false);
        SpecialistServiceResponseDto dto = specialistServiceDto(1L, 10L, 20L, 30L);

        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(specialistServiceRepository.save(entity)).thenReturn(entity);
        when(specialistServiceMapper.toResponseDto(entity)).thenReturn(dto);

        SpecialistServiceResponseDto result = specialistServiceService.restoreById(1L);

        assertEquals(dto, result);
        assertTrue(entity.isActive());
    }

    @Test
    void restoreShouldThrowWhenNotFound() {
        when(specialistServiceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> specialistServiceService.restoreById(1L));
    }

    private BusinessEntity businessEntity(Long id, boolean active) {
        BusinessEntity business = new BusinessEntity();
        business.setId(id);
        business.setName("Business " + id);
        business.setActive(active);
        return business;
    }

    private UserEntity specialistUser(Long id, boolean active) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("Spec");
        user.setLastName("User");
        user.setType(UserType.SPECIALIST);
        user.setActive(active);
        return user;
    }

    private UserEntity clientUser(Long id, boolean active) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("Client");
        user.setLastName("User");
        user.setType(UserType.CLIENT);
        user.setActive(active);
        return user;
    }

    private ServiceEntity serviceEntity(Long id, Long businessId, boolean active) {
        ServiceEntity service = new ServiceEntity();
        service.setId(id);
        service.setName("Service " + id);
        service.setBusiness(businessEntity(businessId, true));
        service.setActive(active);
        return service;
    }

    private SpecialistServiceEntity specialistServiceEntity(Long id, Long businessId, Long specialistId, Long serviceId) {
        SpecialistServiceEntity entity = new SpecialistServiceEntity();
        entity.setId(id);
        entity.setBusiness(businessEntity(businessId, true));
        entity.setSpecialist(specialistUser(specialistId, true));
        entity.setService(serviceEntity(serviceId, businessId, true));
        entity.setActive(true);
        return entity;
    }

    private SpecialistServiceResponseDto specialistServiceDto(Long id, Long businessId, Long specialistId, Long serviceId) {
        return new SpecialistServiceResponseDto(
            id,
            businessId,
            "Business " + businessId,
            specialistId,
            "Spec User",
            serviceId,
            "Service " + serviceId,
            true,
            LocalDateTime.now()
        );
    }
}
