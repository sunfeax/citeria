package com.sunfeax.citeria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.OfferingEntity;

@Repository
public interface OfferingRepository extends JpaRepository<OfferingEntity, Long> {
}
