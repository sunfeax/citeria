package com.sunfeax.citeria.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.exception.ForbiddenException;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.normalizer.AppointmentFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.util.PageableUtil;
import com.sunfeax.citeria.validation.AppointmentValidator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final ServiceRepository serviceRepository;
    private final AppointmentFieldNormalizer appointmentFieldNormalizer;
    private final AppointmentValidator appointmentValidator;
    private final CurrentUserProvider currentUserProvider;

    @Value("${app.booking.paymentWindowHours:24}")
    private long paymentWindowHours;

    @Value("${app.booking.preAppointmentBufferHours:6}")
    private long preAppointmentBufferHours;

    private static final Set<String> SORTABLE = Set.of("startTime", "status", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "startTime");

    @Transactional(readOnly = true)
    public PageResponseDto<AppointmentResponseDto> list(
        AppointmentStatus status,
        Instant from,
        Instant to,
        UUID serviceId,
        Pageable pageable
    ) {
        UserEntity current = currentUserProvider.getCurrentUser();

        List<Specification<AppointmentEntity>> specs = new ArrayList<>();
        if (!currentUserProvider.isAdmin(current)) {
            UUID me = current.getId();
            specs.add((root, query, cb) -> cb.or(
                cb.equal(root.get("client").get("id"), me),
                cb.equal(root.get("specialist").get("id"), me)
            ));
        }
        if (status != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.<Instant>get("startTime"), from));
        }
        if (to != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.<Instant>get("startTime"), to));
        }
        if (serviceId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("service").get("id"), serviceId));
        }

        Pageable sanitized = PageableUtil.sanitizeSort(pageable, SORTABLE, DEFAULT_SORT);
        Page<AppointmentResponseDto> page = appointmentRepository.findAll(Specification.allOf(specs), sanitized)
            .map(appointmentMapper::toResponseDto);

        return PageResponseDto.from(page);
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getById(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertParticipantOrAdmin(appointment);
        return appointmentMapper.toResponseDto(appointment);
    }

    @Transactional
    public AppointmentResponseDto create(AppointmentPostRequestDto request) {
        AppointmentPostRequestDto normalizedRequest = appointmentFieldNormalizer.normalizePostRequest(request);

        UserEntity client = currentUserProvider.getCurrentUser();
        ServiceEntity service = findServiceOrThrow(normalizedRequest.serviceId());

        appointmentValidator.validateCreate(normalizedRequest, client, service);

        AppointmentEntity entity = appointmentMapper.createEntity(normalizedRequest, client, service);
        AppointmentEntity saved = appointmentRepository.save(entity);

        return appointmentMapper.toResponseDto(saved);
    }

    /** Specialist accepts a pending request and opens the client's payment window. */
    @Transactional
    public AppointmentResponseDto accept(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertSpecialistOrAdmin(appointment);
        requireStatus(appointment, AppointmentStatus.PENDING);

        Instant deadline = computePaymentDeadline(appointment.getStartTime(), Instant.now());
        if (!deadline.isAfter(Instant.now())) {
            throw transitionError("Too late to accept: the payment window would already be closed");
        }

        appointment.setStatus(AppointmentStatus.AWAITING_PAYMENT);
        appointment.setPaymentDeadline(deadline);

        return appointmentMapper.toResponseDto(appointmentRepository.save(appointment));
    }

    /** Specialist declines a pending request; the slot is released. */
    @Transactional
    public AppointmentResponseDto reject(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertSpecialistOrAdmin(appointment);
        requireStatus(appointment, AppointmentStatus.PENDING);

        appointment.setStatus(AppointmentStatus.REJECTED);

        return appointmentMapper.toResponseDto(appointmentRepository.save(appointment));
    }

    /** Client pays within the window, confirming the appointment. Payment itself is mocked for now. */
    @Transactional
    public AppointmentResponseDto pay(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertClientOrAdmin(appointment);
        requireStatus(appointment, AppointmentStatus.AWAITING_PAYMENT);

        Instant deadline = appointment.getPaymentDeadline();
        if (deadline != null && Instant.now().isAfter(deadline)) {
            appointment.setStatus(AppointmentStatus.EXPIRED);
            appointmentRepository.save(appointment);
            throw transitionError("Payment window has expired");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setPaymentDeadline(null);

        return appointmentMapper.toResponseDto(appointmentRepository.save(appointment));
    }

    /** Either participant cancels before the appointment takes place; the slot is released. */
    @Transactional
    public AppointmentResponseDto cancel(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertParticipantOrAdmin(appointment);
        requireStatus(
            appointment,
            AppointmentStatus.PENDING,
            AppointmentStatus.AWAITING_PAYMENT,
            AppointmentStatus.CONFIRMED
        );

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setPaymentDeadline(null);

        return appointmentMapper.toResponseDto(appointmentRepository.save(appointment));
    }

    /** Specialist marks a confirmed appointment as completed after it took place. */
    @Transactional
    public AppointmentResponseDto complete(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertSpecialistOrAdmin(appointment);
        requireStatus(appointment, AppointmentStatus.CONFIRMED);

        appointment.setStatus(AppointmentStatus.COMPLETED);

        return appointmentMapper.toResponseDto(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponseDto deleteById(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertParticipantOrAdmin(appointment);

        AppointmentResponseDto deletedAppointment = appointmentMapper.toResponseDto(appointment);
        appointmentRepository.delete(appointment);

        return deletedAppointment;
    }

    /** Releases slots whose payment window has elapsed without payment. */
    @Scheduled(fixedDelayString = "${app.booking.paymentExpiryCheckIntervalMs:300000}")
    @Transactional
    public void expireOverduePayments() {
        List<AppointmentEntity> overdue = appointmentRepository.findByStatusAndPaymentDeadlineBefore(
            AppointmentStatus.AWAITING_PAYMENT, Instant.now()
        );
        if (overdue.isEmpty()) {
            return;
        }

        overdue.forEach(appointment -> {
            appointment.setStatus(AppointmentStatus.EXPIRED);
            appointment.setPaymentDeadline(null);
        });
        appointmentRepository.saveAll(overdue);

        log.debug("Expired {} appointments with an elapsed payment window", overdue.size());
    }

    private Instant computePaymentDeadline(Instant appointmentStart, Instant acceptedAt) {
        Instant byWindow = acceptedAt.plus(Duration.ofHours(paymentWindowHours));
        Instant byBuffer = appointmentStart.minus(Duration.ofHours(preAppointmentBufferHours));
        return byWindow.isBefore(byBuffer) ? byWindow : byBuffer;
    }

    private void requireStatus(AppointmentEntity appointment, AppointmentStatus... allowed) {
        for (AppointmentStatus status : allowed) {
            if (appointment.getStatus() == status) {
                return;
            }
        }
        throw transitionError("Action not allowed for an appointment in status " + appointment.getStatus());
    }

    private RequestValidationException transitionError(String message) {
        return new RequestValidationException(Map.of("status", message));
    }

    private void assertParticipantOrAdmin(AppointmentEntity appointment) {
        UserEntity current = currentUserProvider.getCurrentUser();
        if (currentUserProvider.isAdmin(current)) {
            return;
        }

        UUID currentId = current.getId();
        boolean participant = currentId.equals(appointment.getClient().getId())
            || currentId.equals(appointment.getSpecialist().getId());

        if (!participant) {
            throw new ForbiddenException("You do not have permission to access this appointment");
        }
    }

    private void assertSpecialistOrAdmin(AppointmentEntity appointment) {
        UserEntity current = currentUserProvider.getCurrentUser();
        if (currentUserProvider.isAdmin(current)) {
            return;
        }
        if (!current.getId().equals(appointment.getSpecialist().getId())) {
            throw new ForbiddenException("Only the specialist can perform this action");
        }
    }

    private void assertClientOrAdmin(AppointmentEntity appointment) {
        UserEntity current = currentUserProvider.getCurrentUser();
        if (currentUserProvider.isAdmin(current)) {
            return;
        }
        if (!current.getId().equals(appointment.getClient().getId())) {
            throw new ForbiddenException("Only the client can perform this action");
        }
    }

    private AppointmentEntity findAppointmentOrThrow(UUID id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment with id " + id + " not found"));
    }

    private ServiceEntity findServiceOrThrow(UUID serviceId) {
        return serviceRepository.findById(serviceId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Service with id " + serviceId + " not found")
            );
    }
}
