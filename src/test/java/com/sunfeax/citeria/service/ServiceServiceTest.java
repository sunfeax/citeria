package com.sunfeax.citeria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.ServiceMapper;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.validation.ServiceFieldNormalizer;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private ServiceMapper serviceMapper;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private ServiceFieldNormalizer serviceFieldNormalizer;

    @InjectMocks
    private ServiceService serviceService;

    @Test
    void getAllShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServiceResponseDto dto = serviceResponseDto(1L, 10L, "Consultation");

        when(serviceRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        Page<ServiceResponseDto> result = serviceService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());
    }

    @Test
    void getByIdShouldReturnServiceWhenExists() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServiceResponseDto dto = serviceResponseDto(1L, 10L, "Consultation");

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.getById(1L);

        assertEquals(dto, result);
    }

    @Test
    void getByIdShouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.getById(55L));
    }

    @Test
    void registerShouldSaveServiceWhenRequestIsValid() {
        BusinessEntity business = businessEntity(10L);
        ServicePostRequestDto request = new ServicePostRequestDto(
            10L, "  Consultation  ", "desc", 60, BigDecimal.valueOf(95), "eur"
        );
        ServicePostRequestDto normalized = new ServicePostRequestDto(
            10L, "Consultation", "desc", 60, BigDecimal.valueOf(95), "EUR"
        );
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServiceResponseDto dto = serviceResponseDto(1L, 10L, "Consultation");

        when(serviceFieldNormalizer.normalizePostRequest(request)).thenReturn(normalized);
        when(serviceRepository.existsByBusinessIdAndNameIgnoreCase(10L, "Consultation")).thenReturn(false);
        when(businessRepository.findById(10L)).thenReturn(Optional.of(business));
        when(serviceMapper.createEntity(normalized, business)).thenReturn(entity);
        when(serviceRepository.save(entity)).thenReturn(entity);
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.register(request);

        assertEquals(dto, result);
        verify(serviceRepository).save(entity);
    }

    @Test
    void registerShouldThrowWhenServiceNameExistsInBusiness() {
        ServicePostRequestDto request = new ServicePostRequestDto(
            10L, "Consultation", "desc", 60, BigDecimal.valueOf(95), "EUR"
        );

        when(serviceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(serviceRepository.existsByBusinessIdAndNameIgnoreCase(10L, "Consultation")).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> serviceService.register(request));
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void registerShouldThrowWhenBusinessNotFound() {
        ServicePostRequestDto request = new ServicePostRequestDto(
            10L, "Consultation", "desc", 60, BigDecimal.valueOf(95), "EUR"
        );

        when(serviceFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(serviceRepository.existsByBusinessIdAndNameIgnoreCase(10L, "Consultation")).thenReturn(false);
        when(businessRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.register(request));
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void updateShouldThrowWhenServiceNotFound() {
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, "Consultation", null, null, null, null);

        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, null, null, null, null, null);

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(serviceMapper.hasAnyPatchField(request)).thenReturn(false);
        when(serviceRepository.existsByBusinessIdAndNameIgnoreCaseAndIdNot(10L, "Consultation", 1L)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> serviceService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenDuplicateNameInSameBusiness() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, "Therapy", null, null, null, null);

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(serviceMapper.hasAnyPatchField(request)).thenReturn(true);
        when(serviceRepository.existsByBusinessIdAndNameIgnoreCaseAndIdNot(10L, "Therapy", 1L)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> serviceService.update(1L, request));
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void updateShouldThrowWhenMovingServiceCausesDuplicateName() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServicePatchRequestDto request = new ServicePatchRequestDto(20L, null, null, null, null, null);

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(serviceMapper.hasAnyPatchField(request)).thenReturn(true);
        when(serviceRepository.existsByBusinessIdAndNameIgnoreCaseAndIdNot(20L, "Consultation", 1L)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> serviceService.update(1L, request));
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void updateShouldThrowWhenTargetBusinessNotFound() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServicePatchRequestDto request = new ServicePatchRequestDto(20L, "Therapy", "desc", 90, BigDecimal.valueOf(120), "USD");

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(serviceMapper.hasAnyPatchField(request)).thenReturn(true);
        when(serviceRepository.existsByBusinessIdAndNameIgnoreCaseAndIdNot(20L, "Therapy", 1L)).thenReturn(false);
        when(businessRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.update(1L, request));
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        BusinessEntity targetBusiness = businessEntity(20L);
        ServicePatchRequestDto request = new ServicePatchRequestDto(
            20L,
            "Therapy",
            "new desc",
            90,
            BigDecimal.valueOf(120),
            "USD"
        );
        ServiceResponseDto dto = serviceResponseDto(1L, 20L, "Therapy");

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(serviceMapper.hasAnyPatchField(request)).thenReturn(true);
        when(serviceRepository.existsByBusinessIdAndNameIgnoreCaseAndIdNot(20L, "Therapy", 1L)).thenReturn(false);
        when(businessRepository.findById(20L)).thenReturn(Optional.of(targetBusiness));
        when(serviceMapper.applyPatch(entity, request, targetBusiness)).thenReturn(entity);
        when(serviceRepository.save(entity)).thenReturn(entity);
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.update(1L, request);

        assertEquals(dto, result);
        verify(serviceRepository).save(entity);
    }

    @Test
    void deactivateShouldSetInactiveAndSave() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServiceResponseDto dto = serviceResponseDto(1L, 10L, "Consultation");

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceRepository.save(entity)).thenReturn(entity);
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.deactivateById(1L);

        assertEquals(dto, result);
        assertFalse(entity.isActive());
    }

    @Test
    void deactivateShouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.deactivateById(1L));
    }

    @Test
    void hardDeleteShouldReturnDeletedService() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        ServiceResponseDto dto = serviceResponseDto(1L, 10L, "Consultation");

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.hardDeleteById(1L);

        assertEquals(dto, result);
        verify(serviceRepository).delete(entity);
    }

    @Test
    void hardDeleteShouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.hardDeleteById(1L));
    }

    @Test
    void restoreShouldSetActiveAndSave() {
        ServiceEntity entity = serviceEntity(1L, 10L, "Consultation");
        entity.setActive(false);
        ServiceResponseDto dto = serviceResponseDto(1L, 10L, "Consultation");

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(serviceRepository.save(entity)).thenReturn(entity);
        when(serviceMapper.toResponseDto(entity)).thenReturn(dto);

        ServiceResponseDto result = serviceService.restoreById(1L);

        assertEquals(dto, result);
        assertTrue(entity.isActive());
    }

    @Test
    void restoreShouldThrowWhenServiceNotFound() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceService.restoreById(1L));
    }

    private BusinessEntity businessEntity(Long id) {
        BusinessEntity business = new BusinessEntity();
        business.setId(id);
        business.setName("Business " + id);
        return business;
    }

    private ServiceEntity serviceEntity(Long id, Long businessId, String name) {
        ServiceEntity entity = new ServiceEntity();
        entity.setId(id);
        entity.setBusiness(businessEntity(businessId));
        entity.setName(name);
        entity.setDescription("desc");
        entity.setDurationMinutes(60);
        entity.setPriceAmount(BigDecimal.valueOf(95));
        entity.setCurrency("EUR");
        entity.setActive(true);
        return entity;
    }

    private ServiceResponseDto serviceResponseDto(Long id, Long businessId, String name) {
        return new ServiceResponseDto(
            id,
            businessId,
            name,
            "Business " + businessId,
            "desc",
            BigDecimal.valueOf(95),
            60,
            "EUR",
            true,
            LocalDateTime.now()
        );
    }
}
