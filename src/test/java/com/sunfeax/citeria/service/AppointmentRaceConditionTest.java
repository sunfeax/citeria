package com.sunfeax.citeria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.PaymentMethod;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;

@SpringBootTest
class AppointmentRaceConditionTest {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private SpecialistServiceRepository specialistServiceRepository;

    @Test
    void concurrentCreateForSameSlotShouldPersistOnlyOneAppointment() throws Exception {
        TestFixture fixture = createFixture();
        LocalDateTime start = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusMinutes(60);

        AttemptResult[] results = runConcurrentAttempts(
            bookingTask(fixture.clientOneId(), fixture.specialistServiceId(), start, end),
            bookingTask(fixture.clientTwoId(), fixture.specialistServiceId(), start, end)
        );

        long successCount = countSuccesses(results);
        assertEquals(1L, successCount, "Only one concurrent booking for the same specialist slot should succeed");

        long persisted = appointmentRepository.findAll().stream()
            .filter(appointment -> appointment.getSpecialist().getId().equals(fixture.specialistId()))
            .filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELLED)
            .filter(appointment -> appointment.getStartTime().isBefore(end))
            .filter(appointment -> appointment.getEndTime().isAfter(start))
            .count();
        assertEquals(1L, persisted, "Database must contain exactly one active appointment for this slot");
        assertTrue(
            hasExpectedConflict(results),
            "The failed attempt should end with overlap conflict (validation or DB integrity)"
        );
    }

    @Test
    void concurrentCreateForAdjacentSlotsShouldPersistBothAppointments() throws Exception {
        TestFixture fixture = createFixture();
        LocalDateTime firstStart = LocalDateTime.now().plusDays(6).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime firstEnd = firstStart.plusMinutes(60);
        LocalDateTime secondStart = firstEnd;
        LocalDateTime secondEnd = secondStart.plusMinutes(60);

        AttemptResult[] results = runConcurrentAttempts(
            bookingTask(fixture.clientOneId(), fixture.specialistServiceId(), firstStart, firstEnd),
            bookingTask(fixture.clientTwoId(), fixture.specialistServiceId(), secondStart, secondEnd)
        );

        long successCount = countSuccesses(results);
        assertEquals(2L, successCount, "Adjacent slots should both be accepted");
    }

    private AttemptResult[] runConcurrentAttempts(Callable<AttemptResult> first, Callable<AttemptResult> second)
        throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<AttemptResult> firstFuture = executor.submit(wrap(first, ready, start));
            Future<AttemptResult> secondFuture = executor.submit(wrap(second, ready, start));

            assertTrue(ready.await(5, TimeUnit.SECONDS), "Workers did not get ready in time");
            start.countDown();

            AttemptResult firstResult = firstFuture.get(10, TimeUnit.SECONDS);
            AttemptResult secondResult = secondFuture.get(10, TimeUnit.SECONDS);

            return new AttemptResult[] { firstResult, secondResult };
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(3, TimeUnit.SECONDS);
        }
    }

    private Callable<AttemptResult> wrap(
        Callable<AttemptResult> delegate,
        CountDownLatch ready,
        CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) {
                return AttemptResult.failure(new IllegalStateException("Start latch timeout"));
            }
            try {
                return delegate.call();
            } catch (ExecutionException ex) {
                return AttemptResult.failure(ex.getCause() == null ? ex : ex.getCause());
            } catch (Exception ex) {
                return AttemptResult.failure(ex);
            }
        };
    }

    private Callable<AttemptResult> bookingTask(
        Long clientId,
        Long specialistServiceId,
        LocalDateTime start,
        LocalDateTime end
    ) {
        return () -> {
            appointmentService.create(
                new AppointmentPostRequestDto(
                    clientId,
                    specialistServiceId,
                    start,
                    end,
                    PaymentMethod.ONLINE
                )
            );
            return AttemptResult.success();
        };
    }

    private long countSuccesses(AttemptResult[] results) {
        long success = 0;
        for (AttemptResult result : results) {
            if (result.successful()) {
                success++;
            }
        }
        return success;
    }

    private boolean hasExpectedConflict(AttemptResult[] results) {
        for (AttemptResult result : results) {
            if (!result.successful()) {
                Throwable error = result.error();
                if (error instanceof RequestValidationException) {
                    return true;
                }
                if (containsOverlapConstraint(error)) {
                    return true;
                }
                if (containsMessage(error, "deadlock detected")) {
                    return true;
                }
                if (containsMessage(error, "40P01")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsOverlapConstraint(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains("exclude_overlapping_appointments")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean containsMessage(Throwable throwable, String part) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(part)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private TestFixture createFixture() {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        UserEntity specialist = userRepository.save(
            createUser("Specialist", token + "s", UserType.SPECIALIST)
        );
        UserEntity clientOne = userRepository.save(
            createUser("ClientOne", token + "c1", UserType.CLIENT)
        );
        UserEntity clientTwo = userRepository.save(
            createUser("ClientTwo", token + "c2", UserType.CLIENT)
        );

        BusinessEntity business = new BusinessEntity();
        business.setOwner(specialist);
        business.setName("RaceBusiness-" + token);
        business.setDescription("Race condition test business");
        business.setActive(true);
        business = businessRepository.save(business);

        ServiceEntity service = new ServiceEntity();
        service.setBusiness(business);
        service.setName("RaceService-" + token);
        service.setDescription("Race condition test service");
        service.setDurationMinutes(60);
        service.setPriceAmount(BigDecimal.valueOf(99.00));
        service.setCurrency("EUR");
        service.setActive(true);
        service = serviceRepository.save(service);

        SpecialistServiceEntity specialistService = new SpecialistServiceEntity();
        specialistService.setBusiness(business);
        specialistService.setSpecialist(specialist);
        specialistService.setService(service);
        specialistService.setActive(true);
        specialistService.setCreatedAt(LocalDateTime.now());
        specialistService = specialistServiceRepository.save(specialistService);

        return new TestFixture(
            specialist.getId(),
            specialistService.getId(),
            clientOne.getId(),
            clientTwo.getId()
        );
    }

    private UserEntity createUser(String firstName, String token, UserType type) {
        UserEntity user = new UserEntity();
        user.setFirstName(firstName);
        user.setLastName("RaceTest");
        user.setEmail("race." + token + "@example.com");
        user.setPhone(null);
        user.setPassword("password");
        user.setRole(UserRole.USER);
        user.setType(type);
        user.setActive(true);
        return user;
    }

    private record TestFixture(
        Long specialistId,
        Long specialistServiceId,
        Long clientOneId,
        Long clientTwoId
    ) {}

    private record AttemptResult(boolean successful, Throwable error) {
        private static AttemptResult success() {
            return new AttemptResult(true, null);
        }

        private static AttemptResult failure(Throwable error) {
            return new AttemptResult(false, error);
        }
    }
}
