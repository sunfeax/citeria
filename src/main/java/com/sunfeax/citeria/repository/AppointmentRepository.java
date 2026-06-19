package com.sunfeax.citeria.repository;

import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    Page<AppointmentEntity> findByClientIdOrSpecialistId(UUID clientId, UUID specialistId, Pageable pageable);

    boolean existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNot(
        UUID specialistId,
        LocalDateTime endTime,
        LocalDateTime startTime,
        AppointmentStatus status
    );

    boolean existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNotAndIdNot(
        UUID specialistId,
        LocalDateTime endTime,
        LocalDateTime startTime,
        AppointmentStatus status,
        UUID id
    );
}
