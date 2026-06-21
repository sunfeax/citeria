package com.sunfeax.citeria.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.ServiceEntity;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID>, JpaSpecificationExecutor<ServiceEntity> {
    boolean existsBySpecialistIdAndNameIgnoreCase(UUID specialistId, String name);
    boolean existsBySpecialistIdAndNameIgnoreCaseAndIdNot(UUID specialistId, String name, UUID id);
}
