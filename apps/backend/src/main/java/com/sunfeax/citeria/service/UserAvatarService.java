package com.sunfeax.citeria.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sunfeax.citeria.dto.user.UserAvatarDto;
import com.sunfeax.citeria.entity.UserAvatarEntity;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.UserAvatarRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.security.CurrentUserProvider;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private static final int AVATAR_SIZE_PX = 512;
    private static final double JPEG_QUALITY = 0.85;
    private static final String STORED_CONTENT_TYPE = "image/jpeg";

    private final UserRepository userRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final CurrentUserProvider currentUserProvider;

    @Value("${app.avatar.maxSizeBytes:5242880}")
    private long maxSizeBytes;

    @Value("${app.avatar.maxSourcePixels:40000000}")
    private long maxSourcePixels;

    @Value("${app.avatar.allowedContentTypes:image/png,image/jpeg}")
    private Set<String> allowedContentTypes;

    public void upload(UUID userId, MultipartFile file) {
        currentUserProvider.requireSelfOrAdmin(userId);
        requireUserExists(userId);
        requireAcceptableSize(file);

        byte[] data = toStoredImage(file);

        UserAvatarEntity avatar = userAvatarRepository.findById(userId)
            .orElseGet(() -> {
                UserAvatarEntity created = new UserAvatarEntity();
                created.setUserId(userId);
                return created;
            });
        avatar.setData(data);
        avatar.setContentType(STORED_CONTENT_TYPE);

        userAvatarRepository.save(avatar);
    }

    @Transactional(readOnly = true)
    public UserAvatarDto get(UUID userId) {
        UserAvatarEntity avatar = userAvatarRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Avatar for user " + userId + " not found"));

        return new UserAvatarDto(avatar.getContentType(), avatar.getData(), avatar.getUpdatedAt());
    }

    @Transactional
    public void delete(UUID userId) {
        currentUserProvider.requireSelfOrAdmin(userId);

        if (!userAvatarRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Avatar for user " + userId + " not found");
        }

        userAvatarRepository.deleteById(userId);
    }

    private void requireUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with id " + userId + " not found");
        }
    }

    private void requireAcceptableSize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw invalidFile("Avatar file must not be empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw invalidFile("Avatar must not exceed " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }
    }

    private byte[] toStoredImage(MultipartFile file) {
        try {
            byte[] source = file.getBytes();
            requireSupportedImage(source);
            return renderJpeg(source);
        } catch (RequestValidationException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw invalidFile("Avatar is not a valid image");
        }
    }

    private void requireSupportedImage(byte[] source) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(source))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw unsupportedType();
            }

            ImageReader reader = readers.next();
            try {
                if (!allowedContentTypes.contains(reader.getOriginatingProvider().getMIMETypes()[0])) {
                    throw unsupportedType();
                }

                reader.setInput(stream, true, true);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > maxSourcePixels) {
                    throw invalidFile("Avatar dimensions are too large");
                }
            } finally {
                reader.dispose();
            }
        }
    }

    private byte[] renderJpeg(byte[] source) throws IOException {
        BufferedImage cropped = Thumbnails.of(new ByteArrayInputStream(source))
            .size(AVATAR_SIZE_PX, AVATAR_SIZE_PX)
            .crop(Positions.CENTER)
            .asBufferedImage();

        BufferedImage opaque = new BufferedImage(cropped.getWidth(), cropped.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = opaque.createGraphics();
        graphics.drawImage(cropped, 0, 0, Color.WHITE, null);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thumbnails.of(opaque)
            .scale(1)
            .outputFormat("jpg")
            .outputQuality(JPEG_QUALITY)
            .toOutputStream(output);
        return output.toByteArray();
    }

    private RequestValidationException unsupportedType() {
        return invalidFile("Avatar must be one of: " + String.join(", ", allowedContentTypes));
    }

    private RequestValidationException invalidFile(String message) {
        return new RequestValidationException(Map.of("file", message));
    }
}
