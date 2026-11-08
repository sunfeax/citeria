package com.sunfeax.citeria.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.OfferingEntity;

@Repository
public interface OfferingRepository extends JpaRepository<OfferingEntity, UUID>, JpaSpecificationExecutor<OfferingEntity> {
    boolean existsBySpecialistIdAndNameIgnoreCase(UUID specialistId, String name);
    boolean existsBySpecialistIdAndNameIgnoreCaseAndIdNot(UUID specialistId, String name, UUID id);
    List<OfferingEntity> findBySpecialistIdAndIsActiveTrue(UUID specialistId);
}
