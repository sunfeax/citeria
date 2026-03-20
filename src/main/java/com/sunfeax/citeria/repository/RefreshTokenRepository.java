package com.sunfeax.citeria.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.entity.RefreshTokenEntity;
import com.sunfeax.citeria.entity.UserEntity;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    Optional<RefreshTokenEntity> findByUser(UserEntity user);

    @Modifying
    @Transactional
    void deleteByUser(UserEntity user);

    @Modifying
    @Transactional
    void deleteByTokenHash(String tokenHash);

    @Modifying
    @Transactional
    @Query("""
        update RefreshTokenEntity t
        set t.tokenHash = :newTokenHash, t.expiryDate = :newExpiryDate
        where t.tokenHash = :currentTokenHash
          and t.expiryDate > :now
    """)
    int rotateTokenIfValid(
        @Param("currentTokenHash") String currentTokenHash,
        @Param("newTokenHash") String newTokenHash,
        @Param("newExpiryDate") Instant newExpiryDate,
        @Param("now") Instant now
    );

    @Modifying
    @Transactional
    @Query("delete from RefreshTokenEntity t where t.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
}
