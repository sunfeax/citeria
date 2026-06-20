package com.sunfeax.citeria.repository;

import java.util.UUID;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID>, JpaSpecificationExecutor<AppointmentEntity> {

    boolean existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNot(
        UUID specialistId,
        Instant endTime,
        Instant startTime,
        AppointmentStatus status
    );

    boolean existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNotAndIdNot(
        UUID specialistId,
        Instant endTime,
        Instant startTime,
        AppointmentStatus status,
        UUID id
    );
}
