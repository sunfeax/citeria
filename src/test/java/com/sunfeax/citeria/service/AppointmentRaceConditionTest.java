package com.sunfeax.citeria.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.entity.WorkingHoursEntity;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.repository.WorkingHoursRepository;

@SpringBootTest
class AppointmentRaceConditionTest {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @Test
    void concurrentAcceptForSameSlotShouldConfirmOnlyOne() throws Exception {
        TestFixture fixture = createFixture();
        Instant start = slotStart(5);

        UUID firstId = book(fixture.clientOneEmail(), fixture.serviceId(), start);
        UUID secondId = book(fixture.clientTwoEmail(), fixture.serviceId(), start);

        AttemptResult[] results = runConcurrentAttempts(
            acceptTask(fixture.specialistEmail(), firstId),
            acceptTask(fixture.specialistEmail(), secondId)
        );

        assertEquals(1L, countSuccesses(results), "Only one concurrent accept for the same slot should succeed");

        long blocking = appointmentRepository.findAll().stream()
            .filter(appointment -> appointment.getSpecialist().getId().equals(fixture.specialistId()))
            .filter(appointment -> appointment.getStatus().blocksSlot())
            .filter(appointment -> appointment.getStartTime().isBefore(start.plus(Duration.ofMinutes(60))))
            .filter(appointment -> appointment.getEndTime().isAfter(start))
            .count();
        assertEquals(1L, blocking, "Database must contain exactly one slot-blocking appointment for this slot");
        assertTrue(hasExpectedConflict(results), "The failed accept should end with an overlap conflict");
    }

    @Test
    void concurrentAcceptForAdjacentSlotsShouldConfirmBoth() throws Exception {
        TestFixture fixture = createFixture();
        Instant firstStart = slotStart(6);
        Instant secondStart = firstStart.plus(Duration.ofMinutes(60));

        UUID firstId = book(fixture.clientOneEmail(), fixture.serviceId(), firstStart);
        UUID secondId = book(fixture.clientTwoEmail(), fixture.serviceId(), secondStart);

        AttemptResult[] results = runConcurrentAttempts(
            acceptTask(fixture.specialistEmail(), firstId),
            acceptTask(fixture.specialistEmail(), secondId)
        );

        assertEquals(2L, countSuccesses(results), "Adjacent slots should both be accepted");
    }

    private Instant slotStart(int daysAhead) {

        return LocalDate.now(ZoneOffset.UTC).plusDays(daysAhead).atTime(10, 0).toInstant(ZoneOffset.UTC);
    }

    private UUID book(String clientEmail, UUID serviceId, Instant start) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(clientEmail, null, List.of())
        );
        try {
            return appointmentService.create(new AppointmentPostRequestDto(serviceId, start)).id();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private Callable<AttemptResult> acceptTask(String specialistEmail, UUID appointmentId) {
        return () -> {
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(specialistEmail, null, List.of())
            );
            try {
                appointmentService.accept(appointmentId);
                return AttemptResult.success();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
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
                if (containsMessage(error, "exclude_overlapping_appointments")) {
                    return true;
                }
                if (containsMessage(error, "deadlock detected") || containsMessage(error, "40P01")) {
                    return true;
                }
            }
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

        UserEntity specialist = userRepository.save(createUser("Specialist", token + "s", UserType.SPECIALIST));
        UserEntity clientOne = userRepository.save(createUser("ClientOne", token + "c1", UserType.CLIENT));
        UserEntity clientTwo = userRepository.save(createUser("ClientTwo", token + "c2", UserType.CLIENT));

        ServiceEntity service = new ServiceEntity();
        service.setSpecialist(specialist);
        service.setName("RaceService-" + token);
        service.setDescription("Race condition test service");
        service.setDurationMinutes(60);
        service.setPriceAmount(BigDecimal.valueOf(99.00));
        service.setCurrency("EUR");
        service.setActive(true);
        service = serviceRepository.save(service);

        for (DayOfWeek day : DayOfWeek.values()) {
            WorkingHoursEntity hours = new WorkingHoursEntity();
            hours.setSpecialist(specialist);
            hours.setDayOfWeek(day);
            hours.setStartTime(LocalTime.of(8, 0));
            hours.setEndTime(LocalTime.of(20, 0));
            hours.setActive(true);
            workingHoursRepository.save(hours);
        }

        return new TestFixture(
            specialist.getId(),
            specialist.getEmail(),
            service.getId(),
            clientOne.getEmail(),
            clientTwo.getEmail()
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
        UUID specialistId,
        String specialistEmail,
        UUID serviceId,
        String clientOneEmail,
        String clientTwoEmail
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
