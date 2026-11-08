package com.sunfeax.citeria.service;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import javax.imageio.ImageIO;

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
        ReflectionTestUtils.setField(userAvatarService, "maxSourcePixels", 40_000_000L);
        ReflectionTestUtils.setField(userAvatarService, "allowedContentTypes", Set.of("image/png", "image/jpeg"));
    }

    @Test
    void uploadShouldStoreCenterCroppedJpeg() throws Exception {
        MultipartFile file = upload("image/png", image("png", 1600, 800, BufferedImage.TYPE_INT_RGB, Color.RED));
        when(userRepository.existsById(ID)).thenReturn(true);
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.empty());

        userAvatarService.upload(ID, file);

        UserAvatarEntity saved = savedAvatar();
        BufferedImage stored = ImageIO.read(new ByteArrayInputStream(saved.getData()));
        assertEquals(ID, saved.getUserId());
        assertEquals("image/jpeg", saved.getContentType());
        assertEquals(512, stored.getWidth());
        assertEquals(512, stored.getHeight());
    }

    @Test
    void uploadShouldFlattenTransparencyOntoWhite() throws Exception {
        MultipartFile file = upload("image/png", image("png", 64, 64, BufferedImage.TYPE_INT_ARGB, new Color(0, 0, 0, 0)));
        when(userRepository.existsById(ID)).thenReturn(true);
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.empty());

        userAvatarService.upload(ID, file);

        BufferedImage stored = ImageIO.read(new ByteArrayInputStream(savedAvatar().getData()));
        assertTrue(new Color(stored.getRGB(256, 256)).getRed() > 250);
    }

    @Test
    void uploadShouldApplyExifOrientation() throws Exception {
        MultipartFile file = upload("image/jpeg", withExifRotation(topRedBottomBlueJpeg()));
        when(userRepository.existsById(ID)).thenReturn(true);
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.empty());

        userAvatarService.upload(ID, file);

        BufferedImage stored = ImageIO.read(new ByteArrayInputStream(savedAvatar().getData()));
        Color right = new Color(stored.getRGB(450, 256));
        assertTrue(right.getRed() > right.getBlue());
    }

    @Test
    void uploadShouldReplaceExistingAvatar() throws Exception {
        UserAvatarEntity existing = new UserAvatarEntity(ID, new byte[] {0}, "image/png", Instant.now());
        MultipartFile file = upload("image/png", image("png", 64, 64, BufferedImage.TYPE_INT_RGB, Color.RED));
        when(userRepository.existsById(ID)).thenReturn(true);
        when(userAvatarRepository.findById(ID)).thenReturn(Optional.of(existing));

        userAvatarService.upload(ID, file);

        verify(userAvatarRepository).save(existing);
        assertEquals("image/jpeg", existing.getContentType());
    }

    @Test
    void uploadShouldThrowWhenUserNotFound() {
        MultipartFile file = upload("image/png", new byte[] {1});
        when(userRepository.existsById(ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userAvatarService.upload(ID, file));
        verify(userAvatarRepository, never()).save(any());
    }

    @Test
    void uploadShouldRejectEmptyFile() {
        assertRejected(upload("image/png", new byte[0]));
    }

    @Test
    void uploadShouldRejectTooLargeFile() {
        ReflectionTestUtils.setField(userAvatarService, "maxSizeBytes", 2L);

        assertRejected(upload("image/png", new byte[] {1, 2, 3}));
    }

    @Test
    void uploadShouldRejectNonImageClaimingToBePng() {
        assertRejected(upload("image/png", "<svg onload=alert(1)>".getBytes()));
    }

    @Test
    void uploadShouldRejectUnsupportedFormatClaimingToBePng() throws Exception {
        assertRejected(upload("image/png", image("gif", 8, 8, BufferedImage.TYPE_INT_RGB, Color.RED)));
    }

    @Test
    void uploadShouldRejectTruncatedImage() throws Exception {
        byte[] png = image("png", 64, 64, BufferedImage.TYPE_INT_RGB, Color.RED);

        assertRejected(upload("image/png", Arrays.copyOf(png, 20)));
    }

    @Test
    void uploadShouldRejectTooManyPixels() throws Exception {
        ReflectionTestUtils.setField(userAvatarService, "maxSourcePixels", 100L);

        assertRejected(upload("image/png", image("png", 11, 10, BufferedImage.TYPE_INT_RGB, Color.RED)));
    }

    private void assertRejected(MultipartFile file) {
        when(userRepository.existsById(ID)).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> userAvatarService.upload(ID, file));
        verify(userAvatarRepository, never()).save(any());
    }

    private UserAvatarEntity savedAvatar() {
        ArgumentCaptor<UserAvatarEntity> captor = ArgumentCaptor.forClass(UserAvatarEntity.class);
        verify(userAvatarRepository).save(captor.capture());
        return captor.getValue();
    }

    private MultipartFile upload(String contentType, byte[] content) {
        return new MockMultipartFile("file", "avatar", contentType, content);
    }

    private byte[] image(String format, int width, int height, int type, Color fill) throws IOException {
        BufferedImage image = new BufferedImage(width, height, type);
        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.setColor(fill);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }

    private byte[] topRedBottomBlueJpeg() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 100, 50);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 50, 100, 50);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }

    private byte[] withExifRotation(byte[] jpeg) throws IOException {
        byte[] exif = {
            (byte) 0xFF, (byte) 0xE1, 0, 34,
            'E', 'x', 'i', 'f', 0, 0,
            'M', 'M', 0, 42, 0, 0, 0, 8,
            0, 1,
            0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, 6, 0, 0,
            0, 0, 0, 0
        };

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(jpeg, 0, 2);
        output.write(exif);
        output.write(jpeg, 2, jpeg.length - 2);
        return output.toByteArray();
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
