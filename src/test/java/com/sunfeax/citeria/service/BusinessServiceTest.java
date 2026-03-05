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

import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;
import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.BusinessMapper;
import com.sunfeax.citeria.normalizer.BusinessFieldNormalizer;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.validation.BusinessValidator;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private BusinessMapper businessMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessFieldNormalizer businessFieldNormalizer;

    private BusinessValidator businessValidator;

    private BusinessService businessService;

    @BeforeEach
    void setUp() {
        businessValidator = new BusinessValidator(businessRepository, businessMapper);
        businessService = new BusinessService(
            businessRepository,
            businessMapper,
            userRepository,
            businessFieldNormalizer,
            businessValidator
        );
    }

    @Test
    void getAllShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        BusinessEntity entity = businessEntity(1L, "Alpha");
        BusinessResponseDto dto = businessResponseDto(1L, "Alpha");

        when(businessRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(businessMapper.toResponseDto(entity)).thenReturn(dto);

        Page<BusinessResponseDto> result = businessService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());
    }

    @Test
    void getByIdShouldReturnBusinessWhenExists() {
        BusinessEntity entity = businessEntity(1L, "Alpha");
        BusinessResponseDto dto = businessResponseDto(1L, "Alpha");

        when(businessRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(businessMapper.toResponseDto(entity)).thenReturn(dto);

        BusinessResponseDto result = businessService.getById(1L);

        assertEquals(dto, result);
    }

    @Test
    void getByIdShouldThrowWhenBusinessNotFound() {
        when(businessRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.getById(42L));
    }

    @Test
    void createShouldSaveBusinessWhenRequestIsValid() {
        UserEntity owner = userEntity(10L);
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            10L, "  Alpha Studio  ", "desc", "+34 555 1234", "test@example.com", "site", "address"
        );
        BusinessPostRequestDto normalized = new BusinessPostRequestDto(
            10L, "Alpha Studio", "desc", "345551234", "test@example.com", "site", "address"
        );
        BusinessEntity entity = businessEntity(1L, "Alpha Studio");
        BusinessResponseDto dto = businessResponseDto(1L, "Alpha Studio");

        when(businessFieldNormalizer.normalizePostRequest(request)).thenReturn(normalized);
        when(businessRepository.existsByNameIgnoreCase("Alpha Studio")).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(businessMapper.createEntity(normalized, owner)).thenReturn(entity);
        when(businessRepository.save(entity)).thenReturn(entity);
        when(businessMapper.toResponseDto(entity)).thenReturn(dto);

        BusinessResponseDto result = businessService.create(request);

        assertEquals(dto, result);
        verify(businessRepository).save(entity);
    }

    @Test
    void createShouldThrowWhenNameAlreadyExists() {
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            10L, "Alpha Studio", "desc", null, null, null, null
        );
        BusinessPostRequestDto normalized = new BusinessPostRequestDto(
            10L, "Alpha Studio", "desc", null, null, null, null
        );

        when(businessFieldNormalizer.normalizePostRequest(request)).thenReturn(normalized);
        when(businessRepository.existsByNameIgnoreCase("Alpha Studio")).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> businessService.create(request));
        verify(businessRepository, never()).save(any(BusinessEntity.class));
    }

    @Test
    void createShouldThrowWhenOwnerNotFound() {
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            77L, "Alpha Studio", "desc", null, null, null, null
        );

        when(businessFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(businessRepository.existsByNameIgnoreCase("Alpha Studio")).thenReturn(false);
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.create(request));
        verify(businessRepository, never()).save(any(BusinessEntity.class));
    }

    @Test
    void updateShouldThrowWhenBusinessNotFound() {
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(10L, "Alpha", null, null, null, null, null);

        when(businessRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        BusinessEntity entity = businessEntity(1L, "Alpha");
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(null, null, null, null, null, null, null);

        when(businessRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(businessFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(businessMapper.hasAnyPatchField(request)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> businessService.update(1L, request));
        verify(businessRepository, never()).save(any(BusinessEntity.class));
    }

    @Test
    void updateShouldThrowWhenNameAlreadyExists() {
        BusinessEntity entity = businessEntity(1L, "Alpha");
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(null, "Beta", null, null, null, null, null);

        when(businessRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(businessFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(businessMapper.hasAnyPatchField(request)).thenReturn(true);
        when(businessRepository.existsByNameIgnoreCaseAndIdNot("Beta", 1L)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> businessService.update(1L, request));
        verify(businessRepository, never()).save(any(BusinessEntity.class));
    }

    @Test
    void updateShouldThrowWhenNewOwnerNotFound() {
        BusinessEntity entity = businessEntity(1L, "Alpha");
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(77L, "Beta", null, null, null, null, null);

        when(businessRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(businessFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(businessMapper.hasAnyPatchField(request)).thenReturn(true);
        when(businessRepository.existsByNameIgnoreCaseAndIdNot("Beta", 1L)).thenReturn(false);
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.update(1L, request));
        verify(businessRepository, never()).save(any(BusinessEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        BusinessEntity entity = businessEntity(1L, "Alpha");
        UserEntity newOwner = userEntity(20L);
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(
            20L,
            "Beta",
            "new desc",
            "99887766",
            "new@example.com",
            "new-site",
            "new address"
        );
        BusinessResponseDto dto = businessResponseDto(1L, "Beta");

        when(businessRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(businessFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(businessMapper.hasAnyPatchField(request)).thenReturn(true);
        when(businessRepository.existsByNameIgnoreCaseAndIdNot("Beta", 1L)).thenReturn(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(newOwner));
        when(businessMapper.applyPatch(entity, request, newOwner)).thenReturn(entity);
        when(businessRepository.save(entity)).thenReturn(entity);
        when(businessMapper.toResponseDto(entity)).thenReturn(dto);

        BusinessResponseDto result = businessService.update(1L, request);

        assertEquals(dto, result);
        verify(businessRepository).save(entity);
    }

    @Test
    void deactivateShouldSetInactiveAndSave() {
        BusinessEntity entity = businessEntity(1L, "Alpha");
        BusinessResponseDto dto = businessResponseDto(1L, "Alpha");

        when(businessRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(businessRepository.save(entity)).thenReturn(entity);
        when(businessMapper.toResponseDto(entity)).thenReturn(dto);

        BusinessResponseDto result = businessService.deactivateById(1L);

        assertEquals(dto, result);
        assertFalse(entity.isActive());
    }

    @Test
    void deactivateShouldThrowWhenBusinessNotFound() {
        when(businessRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.deactivateById(1L));
    }

    @Test
    void hardDeleteShouldReturnDeletedBusiness() {
        BusinessEntity entity = businessEntity(1L, "Alpha");
        BusinessResponseDto dto = businessResponseDto(1L, "Alpha");

        when(businessRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(businessMapper.toResponseDto(entity)).thenReturn(dto);

        BusinessResponseDto result = businessService.hardDeleteById(1L);

        assertEquals(dto, result);
        verify(businessRepository).delete(entity);
    }

    @Test
    void hardDeleteShouldThrowWhenBusinessNotFound() {
        when(businessRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.hardDeleteById(1L));
    }

    @Test
    void restoreShouldSetActiveAndSave() {
        BusinessEntity entity = businessEntity(1L, "Alpha");
        entity.setActive(false);
        BusinessResponseDto dto = businessResponseDto(1L, "Alpha");

        when(businessRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(businessRepository.save(entity)).thenReturn(entity);
        when(businessMapper.toResponseDto(entity)).thenReturn(dto);

        BusinessResponseDto result = businessService.restoreById(1L);

        assertEquals(dto, result);
        assertTrue(entity.isActive());
    }

    @Test
    void restoreShouldThrowWhenBusinessNotFound() {
        when(businessRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.restoreById(1L));
    }

    private UserEntity userEntity(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("Owner");
        user.setLastName("Name");
        return user;
    }

    private BusinessEntity businessEntity(Long id, String name) {
        BusinessEntity entity = new BusinessEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setActive(true);
        entity.setOwner(userEntity(10L));
        return entity;
    }

    private BusinessResponseDto businessResponseDto(Long id, String name) {
        return new BusinessResponseDto(
            id,
            name,
            "desc",
            "123",
            "mail@example.com",
            "site",
            "address",
            true,
            10L,
            "Owner Name",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}
