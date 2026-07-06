package com.sunfeax.citeria.repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.UserAvatarEntity;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatarEntity, UUID> {

    @Query("SELECT a.userId FROM UserAvatarEntity a WHERE a.userId IN :userIds")
    Set<UUID> findUserIdsWithAvatar(@Param("userIds") Collection<UUID> userIds);
}
