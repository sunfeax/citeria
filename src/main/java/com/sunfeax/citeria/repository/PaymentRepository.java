package com.sunfeax.citeria.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunfeax.citeria.entity.PaymentEntity;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    boolean existsByAppointmentId(UUID appointmentId);
    boolean existsByAppointmentIdAndIdNot(UUID appointmentId, UUID id);
}
