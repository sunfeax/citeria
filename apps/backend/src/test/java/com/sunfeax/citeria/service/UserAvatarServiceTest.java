package com.sunfeax.citeria.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.sunfeax.citeria.dto.user.UserAvatarDto;
import com.sunfeax.citeria.entity.UserAvatarEntity;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.UserAvatarRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.security.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserAvatarRepository userAvatarRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private UserAvatarService userAvatarService;

    private static final UUID ID = new UUID(0, 1L);

    @BeforeEach
    void setUp() {
        userAvatarService = new UserAvatarService(userRepository, userAvatarRepository, currentUserProvider);
        ReflectionTestUtils.setField(userAvatarService, "maxSizeBytes", 5_242_880L);
        ReflectionTestUtils.setField(userAvatarService, "allowedContentTypes", Set.of("image/png", "image/jpeg"));
    }

    @Test
    void uploadShouldSaveWhenValid() {
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[] {1, 2, 3});
        when(userRepository.existsById(ID)).thenReturn(true);
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.empty());

        userAvatarService.upload(ID, file);

        ArgumentCaptor<UserAvatarEntity> captor = ArgumentCaptor.forClass(UserAvatarEntity.class);
        verify(userAvatarRepository).save(captor.capture());
        UserAvatarEntity saved = captor.getValue();
        assertEquals(ID, saved.getUserId());
        assertEquals("image/png", saved.getContentType());
        assertArrayEquals(new byte[] {1, 2, 3}, saved.getData());
    }

    @Test
    void uploadShouldNormalizeContentTypeToLowerCase() {
        MultipartFile file = new MockMultipartFile("file", "avatar.jpg", "IMAGE/JPEG", new byte[] {9});
        when(userRepository.existsById(ID)).thenReturn(true);
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.empty());

        userAvatarService.upload(ID, file);

        ArgumentCaptor<UserAvatarEntity> captor = ArgumentCaptor.forClass(UserAvatarEntity.class);
        verify(userAvatarRepository).save(captor.capture());
        assertEquals("image/jpeg", captor.getValue().getContentType());
    }

    @Test
    void uploadShouldReplaceExistingAvatar() {
        UserAvatarEntity existing = new UserAvatarEntity(ID, new byte[] {0}, "image/png", Instant.now());
        MultipartFile file = new MockMultipartFile("file", "new.png", "image/png", new byte[] {4, 5});
        when(userRepository.existsById(ID)).thenReturn(true);
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.of(existing));

        userAvatarService.upload(ID, file);

        verify(userAvatarRepository).save(existing);
        assertArrayEquals(new byte[] {4, 5}, existing.getData());
    }

    @Test
    void uploadShouldThrowWhenUserNotFound() {
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[] {1});
        when(userRepository.existsById(ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userAvatarService.upload(ID, file));
        verify(userAvatarRepository, never()).save(any());
    }

    @Test
    void uploadShouldRejectEmptyFile() {
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[0]);
        when(userRepository.existsById(ID)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> userAvatarService.upload(ID, file));
        verify(userAvatarRepository, never()).save(any());
    }

    @Test
    void uploadShouldRejectTooLargeFile() {
        ReflectionTestUtils.setField(userAvatarService, "maxSizeBytes", 2L);
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[] {1, 2, 3});
        when(userRepository.existsById(ID)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> userAvatarService.upload(ID, file));
        verify(userAvatarRepository, never()).save(any());
    }

    @Test
    void uploadShouldRejectUnsupportedContentType() {
        MultipartFile file = new MockMultipartFile("file", "avatar.gif", "image/gif", new byte[] {1, 2, 3});
        when(userRepository.existsById(ID)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> userAvatarService.upload(ID, file));
        verify(userAvatarRepository, never()).save(any());
    }

    @Test
    void getShouldReturnAvatarWhenExists() {
        Instant updatedAt = Instant.parse("2026-01-01T12:00:00Z");
        UserAvatarEntity avatar = new UserAvatarEntity(ID, new byte[] {7, 8}, "image/png", updatedAt);
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.of(avatar));

        UserAvatarDto result = userAvatarService.get(ID);

        assertEquals("image/png", result.contentType());
        assertArrayEquals(new byte[] {7, 8}, result.data());
        assertEquals(updatedAt, result.updatedAt());
    }

    @Test
    void getShouldThrowWhenNotFound() {
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userAvatarService.get(ID));
    }

    @Test
    void deleteShouldRemoveWhenExists() {
        when(userAvatarRepository.existsById(ID)).thenReturn(true);

        userAvatarService.delete(ID);

        verify(userAvatarRepository).deleteById(ID);
    }

    @Test
    void deleteShouldThrowWhenNotFound() {
        when(userAvatarRepository.existsById(ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userAvatarService.delete(ID));
        verify(userAvatarRepository, never()).deleteById(any());
    }
}
