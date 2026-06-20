package com.sunfeax.citeria.enums;

import java.util.Set;

public enum AppointmentStatus {
    PENDING,
    AWAITING_PAYMENT,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    REJECTED,
    EXPIRED;

    /**
     * Terminal statuses that free the time slot, so it returns to the schedule and
     * no longer blocks new bookings (mirrored by the DB exclusion constraint).
     */
    public static final Set<AppointmentStatus> SLOT_RELEASING = Set.of(CANCELLED, REJECTED, EXPIRED);

    public boolean releasesSlot() {
        return SLOT_RELEASING.contains(this);
    }
}
