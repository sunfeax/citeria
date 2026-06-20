-- Payment window deadline (set when the specialist accepts a booking).
ALTER TABLE appointments
  ADD COLUMN payment_deadline TIMESTAMPTZ;

-- Widen the allowed status set for the accept/pay/expire flow.
ALTER TABLE appointments
  DROP CONSTRAINT appointments_status_chk;

ALTER TABLE appointments
  ADD CONSTRAINT appointments_status_chk CHECK (
    status IN ('PENDING', 'AWAITING_PAYMENT', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED', 'EXPIRED')
  );

-- A slot is occupied for every status except the ones that release it.
ALTER TABLE appointments
  DROP CONSTRAINT exclude_overlapping_appointments;

ALTER TABLE appointments
  ADD CONSTRAINT exclude_overlapping_appointments
    EXCLUDE USING gist (
      specialist_id WITH =,
      tstzrange(start_time, end_time, '[)') WITH &&
    )
    WHERE (status NOT IN ('CANCELLED', 'REJECTED', 'EXPIRED'));
