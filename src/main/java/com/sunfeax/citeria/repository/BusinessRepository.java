package com.sunfeax.citeria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.BusinessEntity;

@Repository
public interface BusinessRepository extends JpaRepository<BusinessEntity, Long> {
}
