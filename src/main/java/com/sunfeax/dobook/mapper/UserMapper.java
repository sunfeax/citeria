package com.sunfeax.dobook.mapper;

import org.springframework.stereotype.Component;

import com.sunfeax.dobook.dto.user.UserRegisterRequestDto;
import com.sunfeax.dobook.dto.user.UserResponseDto;
import com.sunfeax.dobook.entity.UserEntity;
import com.sunfeax.dobook.enums.UserRole;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(UserEntity userEntity) {
        return new UserResponseDto(
            userEntity.getId(),
            userEntity.getFirstName(),
            userEntity.getLastName(),
            userEntity.getEmail(),
            userEntity.getPhoneNumber(),
            userEntity.getRole(),
            userEntity.getType(),
            userEntity.getCreatedAt()
        );
    }

    public UserEntity createEntity(UserRegisterRequestDto request) {

        UserEntity entity = new UserEntity();

        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setEmail(request.email());
        entity.setPhoneNumber(request.phoneNumber());
        entity.setPassword(request.password()); // make hash after
        entity.setRole(UserRole.USER);
        entity.setType(request.type());

        return entity;
    }
}
