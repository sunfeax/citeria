-- Flatten the model to a direct specialist -> service relationship.
-- Business and specialist_services are removed; a service now belongs to one specialist.
-- Disposable dev/test rows in the affected tables are cleared so the structural change is
-- clean (the dev seed is reintroduced separately).

DELETE FROM payments;
DELETE FROM appointments;
DELETE FROM working_hours;

-- Detach appointments from the old join table before dropping it.
ALTER TABLE appointments DROP CONSTRAINT fk_appointments_specialist_service;
ALTER TABLE appointments DROP CONSTRAINT fk_appointments_specialist_service_specialist;
DROP INDEX idx_appointments_specialist_service_start_time;

DROP TABLE specialist_services;

DELETE FROM services;

-- Dropping businesses cascades the FK constraints that services/working_hours hold on it.
DROP TABLE businesses CASCADE;

-- services: now owned directly by a specialist.
ALTER TABLE services DROP CONSTRAINT services_business_id_uq;
ALTER TABLE services DROP COLUMN business_id;
ALTER TABLE services ADD COLUMN specialist_id UUID NOT NULL;
ALTER TABLE services ADD CONSTRAINT fk_services_specialist FOREIGN KEY (specialist_id) REFERENCES users(id);
ALTER TABLE services ADD CONSTRAINT services_id_specialist_uq UNIQUE (id, specialist_id);
CREATE INDEX idx_services_specialist_id ON services(specialist_id);

-- working_hours: keyed by specialist only.
ALTER TABLE working_hours DROP CONSTRAINT working_hours_unique;
ALTER TABLE working_hours DROP COLUMN business_id;
DROP INDEX idx_working_hours_specialist_business_active;
ALTER TABLE working_hours ADD CONSTRAINT working_hours_unique UNIQUE (specialist_id, day_of_week);
CREATE INDEX idx_working_hours_specialist_active ON working_hours(specialist_id, is_active);

-- appointments: reference the service directly; specialist_id is retained for the overlap constraint.
ALTER TABLE appointments DROP COLUMN specialist_service_id;
ALTER TABLE appointments ADD COLUMN service_id UUID NOT NULL;
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_service_specialist
  FOREIGN KEY (service_id, specialist_id) REFERENCES services(id, specialist_id);
CREATE INDEX idx_appointments_service_start_time ON appointments(service_id, start_time);
