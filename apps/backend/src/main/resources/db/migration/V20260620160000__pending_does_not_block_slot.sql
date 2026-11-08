

ALTER TABLE appointments DROP CONSTRAINT exclude_overlapping_appointments;

ALTER TABLE appointments
  ADD CONSTRAINT exclude_overlapping_appointments
    EXCLUDE USING gist (
      specialist_id WITH =,
      tstzrange(start_time, end_time, '[)') WITH &&
    )
    WHERE (status IN ('AWAITING_PAYMENT', 'CONFIRMED', 'COMPLETED'));
