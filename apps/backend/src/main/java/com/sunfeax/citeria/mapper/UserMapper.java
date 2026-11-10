package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserUpdateRequestDto;
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
            userEntity.getAbout(),
            userEntity.getRole(),
            userEntity.getType(),
            userEntity.isActive(),
            userEntity.getCreatedAt(),
            userEntity.getAvatarVersion(),
            userEntity.getPassword() != null
        );
    }

    public UserEntity createEntity(RegisterRequestDto request) {

        UserEntity entity = new UserEntity();

        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setEmail(request.email());
        entity.setPhone(request.phone());
        entity.setRole(UserRole.USER);
        entity.setType(request.type());

        return entity;
    }

    public UserEntity applyPatch(UserEntity entity, UserUpdateRequestDto request) {
        if (request.firstName() != null) {
            entity.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            entity.setLastName(request.lastName());
        }
        if (request.email() != null) {
            entity.setEmail(request.email());
        }
        if (request.phone() != null) {
            entity.setPhone(request.phone().isEmpty() ? null : request.phone());
        }
        if (request.about() != null) {
            entity.setAbout(request.about().isEmpty() ? null : request.about());
        }

        return entity;
    }

    public boolean hasAnyPatchField(UserUpdateRequestDto request) {
        return request.firstName() != null
            || request.lastName() != null
            || request.email() != null
            || request.phone() != null
            || request.about() != null;
    }
}
