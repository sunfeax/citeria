package com.sunfeax.citeria.mapper;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.user.UserRegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserRole;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(UserEntity userEntity) {
        return new UserResponseDto(
            userEntity.getId(),
            userEntity.getFirstName(),
            userEntity.getLastName(),
            userEntity.getEmail(),
            userEntity.getPhone(),
            userEntity.getRole(),
            userEntity.getType(),
            userEntity.isActive(),
            userEntity.getCreatedAt()
        );
    }

    public UserEntity toEntity(UserRegisterRequestDto request) {

        UserEntity entity = new UserEntity();

        entity.setFirstName(normalizeName(request.firstName()));
        entity.setLastName(normalizeName(request.lastName()));
        entity.setEmail(request.email().toLowerCase());
        entity.setPhone(request.phone());
        entity.setRole(UserRole.USER);
        entity.setType(request.type());

        return entity;
    }

    private String normalizeName(String value) {
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }
}
