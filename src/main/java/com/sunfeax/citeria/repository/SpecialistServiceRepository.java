package com.sunfeax.citeria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.SpecialistServiceEntity;

@Repository
public interface SpecialistServiceRepository extends JpaRepository<SpecialistServiceEntity, Long> {
    boolean existsByBusinessIdAndSpecialistIdAndServiceId(Long businessId, Long specialistId, Long serviceId);
    boolean existsByBusinessIdAndSpecialistIdAndServiceIdAndIdNot(
        Long businessId,
        Long specialistId,
        Long serviceId,
        Long id
    );
}
