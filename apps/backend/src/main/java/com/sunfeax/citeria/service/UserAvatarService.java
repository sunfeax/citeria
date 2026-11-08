package com.sunfeax.citeria.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final UserRepository userRepository;
    private final UserAvatarRepository userAvatarRepository;
    private final CurrentUserProvider currentUserProvider;

    @Value("${app.avatar.maxSizeBytes:5242880}")
    private long maxSizeBytes;

    @Value("${app.avatar.allowedContentTypes:image/png,image/jpeg}")
    private Set<String> allowedContentTypes;

    @Transactional
    public void upload(UUID userId, MultipartFile file) {
        currentUserProvider.requireSelfOrAdmin(userId);
        requireUserExists(userId);

        String contentType = validate(file);

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new RequestValidationException(Map.of("file", "Could not read the uploaded file"));
        }

        UserAvatarEntity avatar = userAvatarRepository.findById(userId)
            .orElseGet(() -> {
                UserAvatarEntity created = new UserAvatarEntity();
                created.setUserId(userId);
                return created;
            });
        avatar.setData(data);
        avatar.setContentType(contentType);

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

    /**
     * Validates the uploaded file and returns the normalized (lower-cased) content type.
     */
    private String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RequestValidationException(Map.of("file", "Avatar file must not be empty"));
        }
        if (file.getSize() > maxSizeBytes) {
            throw new RequestValidationException(Map.of("file",
                "Avatar must not exceed " + (maxSizeBytes / (1024 * 1024)) + " MB"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            throw new RequestValidationException(Map.of("file",
                "Avatar must be one of: " + String.join(", ", allowedContentTypes)));
        }

        return contentType.toLowerCase();
    }
}
