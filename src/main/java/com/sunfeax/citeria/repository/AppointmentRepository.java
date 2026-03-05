package com.sunfeax.citeria.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.AppointmentEntity;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    boolean existsBySpecialistServiceIdAndStartTimeLessThanAndEndTimeGreaterThan(
        Long specialistServiceId,
        LocalDateTime endTime,
        LocalDateTime startTime
    );

    boolean existsBySpecialistServiceIdAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
        Long specialistServiceId,
        LocalDateTime endTime,
        LocalDateTime startTime,
        Long id
    );
}
