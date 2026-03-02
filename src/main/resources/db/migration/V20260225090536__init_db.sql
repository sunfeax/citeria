CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  phone VARCHAR(20) UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  type VARCHAR(20) NOT NULL DEFAULT 'CLIENT',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT users_role_chk CHECK (role IN ('USER', 'ADMIN')),
  CONSTRAINT users_type_chk CHECK (type IN ('CLIENT', 'SPECIALIST'))
);

CREATE TABLE businesses (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  owner_user_id BIGINT NOT NULL,
  description TEXT,
  phone VARCHAR(20),
  email VARCHAR(100),
  website VARCHAR(255),
  address VARCHAR(255),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_businesses_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

CREATE TABLE services (
  id BIGSERIAL PRIMARY KEY,
  business_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  duration_minutes INTEGER NOT NULL,
  price_amount NUMERIC(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'EUR',
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_services_business FOREIGN KEY (business_id) REFERENCES businesses(id),
  CONSTRAINT services_duration_chk CHECK (duration_minutes BETWEEN 15 AND 480),
  CONSTRAINT services_price_non_negative_chk CHECK (price_amount >= 0)
);

CREATE TABLE offerings (
  id BIGSERIAL PRIMARY KEY,
  business_id BIGINT NOT NULL,
  specialist_id BIGINT NOT NULL,
  service_id BIGINT NOT NULL,
  price_amount NUMERIC(12,2) NOT NULL,
  duration_minutes INTEGER NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_offerings_business FOREIGN KEY (business_id) REFERENCES businesses(id),
  CONSTRAINT fk_offerings_specialist FOREIGN KEY (specialist_id) REFERENCES users(id),
  CONSTRAINT fk_offerings_service FOREIGN KEY (service_id) REFERENCES services(id),
  CONSTRAINT offerings_price_non_negative_chk CHECK (price_amount >= 0),
  CONSTRAINT offerings_duration_chk CHECK (duration_minutes BETWEEN 15 AND 480),
  CONSTRAINT offerings_unique UNIQUE (specialist_id, service_id)
);

CREATE TABLE appointments (
  id BIGSERIAL PRIMARY KEY,
  client_id BIGINT NOT NULL,
  offering_id BIGINT NOT NULL,
  start_time TIMESTAMPTZ NOT NULL,
  end_time TIMESTAMPTZ NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  payment_method VARCHAR(20) NOT NULL DEFAULT 'ONLINE',
  price_amount NUMERIC(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'EUR',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_appointments_client FOREIGN KEY (client_id) REFERENCES users(id),
  CONSTRAINT fk_appointments_offering FOREIGN KEY (offering_id) REFERENCES offerings(id),
  CONSTRAINT appointments_end_after_start_chk CHECK (end_time > start_time),
  CONSTRAINT appointments_price_amount_non_negative_chk CHECK (price_amount >= 0),
  CONSTRAINT appointments_status_chk CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
  CONSTRAINT appointments_payment_method_chk CHECK (payment_method IN ('ONLINE', 'ON_SITE'))
);

CREATE TABLE payments (
  id BIGSERIAL PRIMARY KEY,
  appointment_id BIGINT NOT NULL UNIQUE,
  amount NUMERIC(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'EUR',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT payments_amount_non_negative_chk CHECK (amount >= 0),
  CONSTRAINT payments_status_chk CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
  CONSTRAINT fk_payments_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE INDEX idx_businesses_owner_user_id ON businesses(owner_user_id);
CREATE INDEX idx_services_business_id ON services(business_id);
CREATE INDEX idx_offerings_service_id ON offerings(service_id);

CREATE INDEX idx_appointments_client_start_time ON appointments(client_id, start_time);
CREATE INDEX idx_appointments_offering_start_time ON appointments(offering_id, start_time);
CREATE INDEX idx_appointments_status_start_time ON appointments(status, start_time);

CREATE INDEX idx_payments_status ON payments(status);
