package com.sunfeax.citeria.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.user.UserPostRequestDto;
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

    public UserEntity createEntity(UserPostRequestDto request) {

        UserEntity entity = new UserEntity();

        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setEmail(request.email());
        entity.setPhone(request.phone());
        entity.setRole(UserRole.USER);
        entity.setType(request.type());

        return entity;
    }
}
