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
     * Statuses that occupy the time slot (mirrored by the DB exclusion constraint).
     * PENDING does NOT block: a request does not hold the slot until the specialist accepts,
     * so the slot stays visible and several clients may request it (preventing slot-squatting).
     */
    public static final Set<AppointmentStatus> SLOT_BLOCKING = Set.of(AWAITING_PAYMENT, CONFIRMED, COMPLETED);

    /**
     * A client's live bookings — used to stop the same client from holding overlapping
     * or duplicate bookings.
     */
    public static final Set<AppointmentStatus> CLIENT_ACTIVE = Set.of(PENDING, AWAITING_PAYMENT, CONFIRMED);

    public boolean blocksSlot() {
        return SLOT_BLOCKING.contains(this);
    }
}
