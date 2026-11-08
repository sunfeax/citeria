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

    List<AppointmentEntity> findBySpecialistIdAndStatusInAndEndTimeGreaterThanAndStartTimeLessThan(
        UUID specialistId,
        Collection<AppointmentStatus> statuses,
        Instant rangeStart,
        Instant rangeEnd
    );

    List<AppointmentEntity> findBySpecialistIdAndStatusAndEndTimeGreaterThanAndStartTimeLessThan(
        UUID specialistId,
        AppointmentStatus status,
        Instant rangeStart,
        Instant rangeEnd
    );

    boolean existsByClientIdAndStatusInAndEndTimeGreaterThanAndStartTimeLessThan(
        UUID clientId,
        Collection<AppointmentStatus> statuses,
        Instant rangeStart,
        Instant rangeEnd
    );

    long countByClientIdAndStatus(UUID clientId, AppointmentStatus status);

    List<AppointmentEntity> findByStatusAndPaymentDeadlineBefore(AppointmentStatus status, Instant cutoff);

    List<AppointmentEntity> findByStatusAndStartTimeBefore(AppointmentStatus status, Instant cutoff);
}
