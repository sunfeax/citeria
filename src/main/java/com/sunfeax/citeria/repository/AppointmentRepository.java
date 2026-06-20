package com.sunfeax.citeria.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID>, JpaSpecificationExecutor<AppointmentEntity> {

    List<AppointmentEntity> findBySpecialistIdAndStatusNotInAndEndTimeGreaterThanAndStartTimeLessThan(
        UUID specialistId,
        Collection<AppointmentStatus> statuses,
        Instant rangeStart,
        Instant rangeEnd
    );

    List<AppointmentEntity> findByStatusAndPaymentDeadlineBefore(AppointmentStatus status, Instant cutoff);

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
