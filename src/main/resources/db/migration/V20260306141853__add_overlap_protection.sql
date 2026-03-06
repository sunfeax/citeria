CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE specialist_services
  ADD CONSTRAINT specialist_services_id_specialist_uq UNIQUE (id, specialist_id);

ALTER TABLE appointments
  ADD COLUMN specialist_id BIGINT;

UPDATE appointments a
SET specialist_id = ss.specialist_id
FROM specialist_services ss
WHERE a.specialist_service_id = ss.id;

ALTER TABLE appointments
  ALTER COLUMN specialist_id SET NOT NULL;

ALTER TABLE appointments
  ADD CONSTRAINT fk_appointments_specialist FOREIGN KEY (specialist_id) REFERENCES users(id);

ALTER TABLE appointments
  ADD CONSTRAINT fk_appointments_specialist_service_specialist
    FOREIGN KEY (specialist_service_id, specialist_id) REFERENCES specialist_services(id, specialist_id);

ALTER TABLE appointments
  ADD CONSTRAINT exclude_overlapping_appointments
    EXCLUDE USING gist (
      specialist_id WITH =,
      tstzrange(start_time, end_time, '[)') WITH &&
    )
    WHERE (status <> 'CANCELLED');
