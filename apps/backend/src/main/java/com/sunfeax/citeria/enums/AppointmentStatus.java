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

    public static final Set<AppointmentStatus> SLOT_BLOCKING = Set.of(AWAITING_PAYMENT, CONFIRMED, COMPLETED);

    public static final Set<AppointmentStatus> CLIENT_ACTIVE = Set.of(PENDING, AWAITING_PAYMENT, CONFIRMED);

    public boolean blocksSlot() {
        return SLOT_BLOCKING.contains(this);
    }
}
