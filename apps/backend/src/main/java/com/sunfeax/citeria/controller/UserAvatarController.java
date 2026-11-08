package com.sunfeax.citeria.controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sunfeax.citeria.dto.user.UserAvatarDto;
import com.sunfeax.citeria.service.UserAvatarService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/{id}/avatar")
@RequiredArgsConstructor
public class UserAvatarController {

    private final UserAvatarService userAvatarService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
        @PathVariable UUID id,
        @RequestParam("file") MultipartFile file
    ) {
        userAvatarService.upload(id, file);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<byte[]> get(@PathVariable UUID id) {
        UserAvatarDto avatar = userAvatarService.get(id);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(avatar.contentType()))
            .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
            .lastModified(avatar.updatedAt())
            .body(avatar.data());
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userAvatarService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
