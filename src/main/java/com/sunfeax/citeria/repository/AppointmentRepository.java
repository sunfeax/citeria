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

    // Slot-blocking appointments overlapping a window (for slot computation).
    List<AppointmentEntity> findBySpecialistIdAndStatusInAndEndTimeGreaterThanAndStartTimeLessThan(
        UUID specialistId,
        Collection<AppointmentStatus> statuses,
        Instant rangeStart,
        Instant rangeEnd
    );

    // Pending requests for one specialist overlapping a window (for auto-rejecting siblings on accept).
    List<AppointmentEntity> findBySpecialistIdAndStatusAndEndTimeGreaterThanAndStartTimeLessThan(
        UUID specialistId,
        AppointmentStatus status,
        Instant rangeStart,
        Instant rangeEnd
    );

    // Does this client already hold an overlapping live booking?
    boolean existsByClientIdAndStatusInAndEndTimeGreaterThanAndStartTimeLessThan(
        UUID clientId,
        Collection<AppointmentStatus> statuses,
        Instant rangeStart,
        Instant rangeEnd
    );

    long countByClientIdAndStatus(UUID clientId, AppointmentStatus status);

    List<AppointmentEntity> findByStatusAndPaymentDeadlineBefore(AppointmentStatus status, Instant cutoff);
}
