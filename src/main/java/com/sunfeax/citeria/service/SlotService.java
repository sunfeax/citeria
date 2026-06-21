package com.sunfeax.citeria.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.slot.SlotResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.entity.WorkingHoursEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.repository.WorkingHoursRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final ServiceRepository serviceRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final AppointmentRepository appointmentRepository;

    @Value("${app.booking.zone:UTC}")
    private String bookingZone;

    @Value("${app.booking.defaultHorizonDays:14}")
    private int defaultHorizonDays;

    @Value("${app.booking.maxHorizonDays:60}")
    private int maxHorizonDays;

    @Transactional(readOnly = true)
    public List<SlotResponseDto> getAvailableSlots(UUID serviceId, LocalDate from, LocalDate to) {
        ServiceEntity service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + serviceId + " not found"));

        ZoneId zone = ZoneId.of(bookingZone);

        // Bookings can only start from the next day onward.
        LocalDate earliest = LocalDate.now(zone).plusDays(1);
        LocalDate firstDay = (from == null || from.isBefore(earliest)) ? earliest : from;
        LocalDate lastDay = (to == null) ? firstDay.plusDays(defaultHorizonDays) : to;

        // Cap the window so a single request cannot scan an unbounded range.
        LocalDate maxDay = firstDay.plusDays(maxHorizonDays);
        if (lastDay.isAfter(maxDay)) {
            lastDay = maxDay;
        }

        if (lastDay.isBefore(firstDay) || !service.isActive()) {
            return List.of();
        }

        UserEntity specialist = service.getSpecialist();
        Duration duration = Duration.ofMinutes(service.getDurationMinutes());

        List<WorkingHoursEntity> workingHours =
            workingHoursRepository.findBySpecialistIdAndIsActiveTrue(specialist.getId());
        if (workingHours.isEmpty()) {
            return List.of();
        }

        Instant rangeStart = firstDay.atStartOfDay(zone).toInstant();
        Instant rangeEnd = lastDay.plusDays(1).atStartOfDay(zone).toInstant();
        List<AppointmentEntity> bookedAppointments =
            appointmentRepository.findBySpecialistIdAndStatusNotInAndEndTimeGreaterThanAndStartTimeLessThan(
                specialist.getId(), AppointmentStatus.SLOT_RELEASING, rangeStart, rangeEnd
            );

        List<SlotResponseDto> slots = new ArrayList<>();
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            for (WorkingHoursEntity window : workingHours) {
                if (window.getDayOfWeek() != date.getDayOfWeek()) {
                    continue;
                }
                addSlotsForWindow(slots, date, window, zone, duration, bookedAppointments);
            }
        }

        return slots;
    }

    private void addSlotsForWindow(
        List<SlotResponseDto> slots,
        LocalDate date,
        WorkingHoursEntity window,
        ZoneId zone,
        Duration duration,
        List<AppointmentEntity> bookedAppointments
    ) {
        Instant cursor = ZonedDateTime.of(date, window.getStartTime(), zone).toInstant();
        Instant windowEnd = ZonedDateTime.of(date, window.getEndTime(), zone).toInstant();

        // Round down: a trailing gap shorter than one slot is dropped.
        while (!cursor.plus(duration).isAfter(windowEnd)) {
            Instant slotEnd = cursor.plus(duration);
            if (isFree(cursor, slotEnd, bookedAppointments)) {
                slots.add(new SlotResponseDto(cursor, slotEnd));
            }
            cursor = slotEnd;
        }
    }

    private boolean isFree(Instant start, Instant end, List<AppointmentEntity> bookedAppointments) {
        for (AppointmentEntity appointment : bookedAppointments) {
            boolean overlaps = appointment.getStartTime().isBefore(end)
                && appointment.getEndTime().isAfter(start);
            if (overlaps) {
                return false;
            }
        }
        return true;
    }
}
