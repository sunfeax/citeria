package com.sunfeax.citeria.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.SpecialistServiceEntity;

@Repository
public interface SpecialistServiceRepository extends JpaRepository<SpecialistServiceEntity, UUID> {
    boolean existsByBusinessIdAndSpecialistIdAndServiceId(UUID businessId, UUID specialistId, UUID serviceId);
    boolean existsByBusinessIdAndSpecialistIdAndServiceIdAndIdNot(
        UUID businessId,
        UUID specialistId,
        UUID serviceId,
        UUID id
    );
}
