CREATE TABLE working_hours (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  specialist_id UUID NOT NULL,
  business_id UUID NOT NULL,
  day_of_week VARCHAR(9) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_working_hours_specialist FOREIGN KEY (specialist_id) REFERENCES users(id),
  CONSTRAINT fk_working_hours_business FOREIGN KEY (business_id) REFERENCES businesses(id),
  CONSTRAINT working_hours_end_after_start_chk CHECK (end_time > start_time),
  CONSTRAINT working_hours_day_chk CHECK (
    day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')
  ),
  CONSTRAINT working_hours_unique UNIQUE (business_id, specialist_id, day_of_week)
);

CREATE INDEX idx_working_hours_specialist_business_active
  ON working_hours(specialist_id, business_id, is_active);
