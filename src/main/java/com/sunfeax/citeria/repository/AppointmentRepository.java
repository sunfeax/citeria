package com.sunfeax.citeria.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    boolean existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNot(
        Long specialistId,
        LocalDateTime endTime,
        LocalDateTime startTime,
        AppointmentStatus status
    );

    boolean existsBySpecialistIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusNotAndIdNot(
        Long specialistId,
        LocalDateTime endTime,
        LocalDateTime startTime,
        AppointmentStatus status,
        Long id
    );
}
