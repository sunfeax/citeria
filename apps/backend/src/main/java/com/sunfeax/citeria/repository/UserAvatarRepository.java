package com.sunfeax.citeria.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.UserAvatarEntity;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatarEntity, UUID> {
}
