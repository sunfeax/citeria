package com.sunfeax.citeria.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.WorkingHoursEntity;

@Repository
public interface WorkingHoursRepository
    extends JpaRepository<WorkingHoursEntity, UUID>, JpaSpecificationExecutor<WorkingHoursEntity> {

    boolean existsBySpecialistIdAndDayOfWeek(UUID specialistId, DayOfWeek dayOfWeek);

    List<WorkingHoursEntity> findBySpecialistIdAndIsActiveTrue(UUID specialistId);
}
