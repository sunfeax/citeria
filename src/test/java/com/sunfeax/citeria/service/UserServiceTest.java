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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserPatchRequestDto;
import com.sunfeax.citeria.dto.user.UserPostRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.validation.UserFieldNormalizer;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserFieldNormalizer userFieldNormalizer;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllShouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        UserEntity entity = userEntity(1L);
        UserResponseDto dto = userResponseDto(1L);

        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(userMapper.toResponseDto(entity)).thenReturn(dto);

        Page<UserResponseDto> result = userService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(dto, result.getContent().getFirst());
    }

    @Test
    void getByIdShouldReturnUserWhenExists() {
        UserEntity entity = userEntity(1L);
        UserResponseDto dto = userResponseDto(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toResponseDto(entity)).thenReturn(dto);

        UserResponseDto result = userService.getById(1L);

        assertEquals(dto, result);
    }

    @Test
    void getByIdShouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        UserPostRequestDto request = new UserPostRequestDto(
            "John", "Snow", "john@example.com", "1234567", "Password!", UserType.CLIENT
        );

        when(userFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        when(userRepository.existsByPhone("1234567")).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerShouldThrowWhenPhoneAlreadyExists() {
        UserPostRequestDto request = new UserPostRequestDto(
            "John", "Snow", "john@example.com", "1234567", "Password!", UserType.CLIENT
        );

        when(userFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567")).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerShouldEncodePasswordAndSave() {
        UserPostRequestDto request = new UserPostRequestDto(
            "John", "Snow", "john@example.com", "1234567", "Password!", UserType.CLIENT
        );
        UserEntity entity = userEntity(1L);
        UserResponseDto dto = userResponseDto(1L);

        when(userFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567")).thenReturn(false);
        when(userMapper.createEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("Password!")).thenReturn("encoded");
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponseDto(entity)).thenReturn(dto);

        UserResponseDto result = userService.register(request);

        assertEquals(dto, result);
        assertEquals("encoded", entity.getPassword());
    }

    @Test
    void updateShouldThrowWhenUserNotFound() {
        UserPatchRequestDto request = new UserPatchRequestDto("John", null, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenNoFieldsProvided() {
        UserEntity entity = userEntity(1L);
        UserPatchRequestDto request = new UserPatchRequestDto(null, null, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(userMapper.hasAnyPatchField(request)).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> userService.update(1L, request));
    }

    @Test
    void updateShouldThrowWhenEmailAlreadyTaken() {
        UserEntity entity = userEntity(1L);
        UserPatchRequestDto request = new UserPatchRequestDto(null, null, "new@example.com", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(userMapper.hasAnyPatchField(request)).thenReturn(true);
        when(userRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> userService.update(1L, request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateShouldThrowWhenPhoneAlreadyTaken() {
        UserEntity entity = userEntity(1L);
        UserPatchRequestDto request = new UserPatchRequestDto(null, null, null, "99887766", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(userMapper.hasAnyPatchField(request)).thenReturn(true);
        when(userRepository.existsByPhoneAndIdNot("99887766", 1L)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> userService.update(1L, request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateShouldApplyPatchAndSaveWhenRequestIsValid() {
        UserEntity entity = userEntity(1L);
        UserPatchRequestDto request = new UserPatchRequestDto("Jane", null, "jane@example.com", "99887766", UserType.SPECIALIST);
        UserResponseDto dto = userResponseDto(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userFieldNormalizer.normalizePatchRequest(request)).thenReturn(request);
        when(userMapper.hasAnyPatchField(request)).thenReturn(true);
        when(userRepository.existsByEmailAndIdNot("jane@example.com", 1L)).thenReturn(false);
        when(userRepository.existsByPhoneAndIdNot("99887766", 1L)).thenReturn(false);
        when(userMapper.applyPatch(entity, request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponseDto(entity)).thenReturn(dto);

        UserResponseDto result = userService.update(1L, request);

        assertEquals(dto, result);
        verify(userRepository).save(entity);
    }

    @Test
    void changePasswordShouldThrowWhenUserNotFound() {
        UserChangePasswordRequestDto request = new UserChangePasswordRequestDto("OldPassword!", "NewPassword!");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void changePasswordShouldThrowWhenCurrentPasswordIsWrong() {
        UserEntity entity = userEntity(1L);
        entity.setPassword("encoded-old");
        UserChangePasswordRequestDto request = new UserChangePasswordRequestDto("WrongOld!", "NewPassword!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("WrongOld!", "encoded-old")).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void changePasswordShouldThrowWhenNewPasswordEqualsCurrent() {
        UserEntity entity = userEntity(1L);
        entity.setPassword("encoded-old");
        UserChangePasswordRequestDto request = new UserChangePasswordRequestDto("OldPassword!", "OldPassword!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("OldPassword!", "encoded-old")).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> userService.changePassword(1L, request));
        verify(passwordEncoder, never()).encode(any(String.class));
    }

    @Test
    void changePasswordShouldUpdatePasswordWhenCurrentMatches() {
        UserEntity entity = userEntity(1L);
        entity.setPassword("encoded-old");
        UserChangePasswordRequestDto request = new UserChangePasswordRequestDto("OldPassword!", "NewPassword!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("OldPassword!", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword!")).thenReturn("encoded-new");

        userService.changePassword(1L, request);

        assertEquals("encoded-new", entity.getPassword());
    }

    @Test
    void deactivateShouldSetInactiveAndSave() {
        UserEntity entity = userEntity(1L);
        UserResponseDto dto = userResponseDto(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponseDto(entity)).thenReturn(dto);

        UserResponseDto result = userService.deactivateById(1L);

        assertEquals(dto, result);
        assertFalse(entity.isActive());
    }

    @Test
    void deactivateShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deactivateById(1L));
    }

    @Test
    void hardDeleteShouldDeleteAndReturnUserDto() {
        UserEntity entity = userEntity(1L);
        UserResponseDto dto = userResponseDto(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toResponseDto(entity)).thenReturn(dto);

        UserResponseDto result = userService.hardDeleteById(1L);

        assertEquals(dto, result);
        verify(userRepository).delete(entity);
    }

    @Test
    void hardDeleteShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.hardDeleteById(1L));
    }

    @Test
    void restoreShouldSetActiveAndSave() {
        UserEntity entity = userEntity(1L);
        entity.setActive(false);
        UserResponseDto dto = userResponseDto(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponseDto(entity)).thenReturn(dto);

        UserResponseDto result = userService.restoreById(1L);

        assertEquals(dto, result);
        assertTrue(entity.isActive());
    }

    @Test
    void restoreShouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.restoreById(1L));
    }

    private UserEntity userEntity(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("John");
        user.setLastName("Snow");
        user.setEmail("john@example.com");
        user.setPhone("1234567");
        user.setRole(UserRole.USER);
        user.setType(UserType.CLIENT);
        user.setActive(true);
        return user;
    }

    private UserResponseDto userResponseDto(Long id) {
        return new UserResponseDto(
            id,
            "John",
            "Snow",
            "john@example.com",
            "1234567",
            UserRole.USER,
            UserType.CLIENT,
            true,
            LocalDateTime.now()
        );
    }
}
