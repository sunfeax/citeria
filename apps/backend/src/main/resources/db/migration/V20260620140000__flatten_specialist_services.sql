

DELETE FROM payments;
DELETE FROM appointments;
DELETE FROM working_hours;

ALTER TABLE appointments DROP CONSTRAINT fk_appointments_specialist_service;
ALTER TABLE appointments DROP CONSTRAINT fk_appointments_specialist_service_specialist;
DROP INDEX idx_appointments_specialist_service_start_time;

DROP TABLE specialist_services;

DELETE FROM services;

DROP TABLE businesses CASCADE;

ALTER TABLE services DROP CONSTRAINT services_business_id_uq;
ALTER TABLE services DROP COLUMN business_id;
ALTER TABLE services ADD COLUMN specialist_id UUID NOT NULL;
ALTER TABLE services ADD CONSTRAINT fk_services_specialist FOREIGN KEY (specialist_id) REFERENCES users(id);
ALTER TABLE services ADD CONSTRAINT services_id_specialist_uq UNIQUE (id, specialist_id);
CREATE INDEX idx_services_specialist_id ON services(specialist_id);

ALTER TABLE working_hours DROP CONSTRAINT working_hours_unique;
DROP INDEX idx_working_hours_specialist_business_active;
ALTER TABLE working_hours DROP COLUMN business_id;
ALTER TABLE working_hours ADD CONSTRAINT working_hours_unique UNIQUE (specialist_id, day_of_week);
CREATE INDEX idx_working_hours_specialist_active ON working_hours(specialist_id, is_active);

ALTER TABLE appointments DROP COLUMN specialist_service_id;
ALTER TABLE appointments ADD COLUMN service_id UUID NOT NULL;
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_service_specialist
  FOREIGN KEY (service_id, specialist_id) REFERENCES services(id, specialist_id);
CREATE INDEX idx_appointments_service_start_time ON appointments(service_id, start_time);
