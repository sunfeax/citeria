
ALTER TABLE appointments
  ADD COLUMN payment_deadline TIMESTAMPTZ;

ALTER TABLE appointments
  DROP CONSTRAINT appointments_status_chk;

ALTER TABLE appointments
  ADD CONSTRAINT appointments_status_chk CHECK (
    status IN ('PENDING', 'AWAITING_PAYMENT', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED', 'EXPIRED')
  );

ALTER TABLE appointments
  DROP CONSTRAINT exclude_overlapping_appointments;

ALTER TABLE appointments
  ADD CONSTRAINT exclude_overlapping_appointments
    EXCLUDE USING gist (
      specialist_id WITH =,
      tstzrange(start_time, end_time, '[)') WITH &&
    )
    WHERE (status NOT IN ('CANCELLED', 'REJECTED', 'EXPIRED'));
