INSERT INTO users (first_name, last_name, email, phone, password, role, type)
VALUES
  ('Anna', 'Petrenko', 'anna.petrenko@example.com', '+15125550101', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQ8m', 'USER', 'CLIENT'),
  ('Dmitry', 'Sokolov', 'dmitry.sokolov@example.com', '+15035550102', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQ8m', 'USER', 'CLIENT'),
  ('Olga', 'Petrova', 'olga.psychologist@example.com', '+13125550103', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQ8m', 'USER', 'SPECIALIST'),
  ('Roman', 'Kovalenko', 'roman.coach@example.com', '+12065550104', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQ8m', 'ADMIN', 'SPECIALIST'),
  ('Maria', 'Ivanova', 'maria.client@example.com', '+14155550105', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqqQ8m', 'USER', 'CLIENT');

INSERT INTO businesses (owner_user_id, name, description, phone, email, website, address, is_active)
VALUES
  (
    (SELECT id FROM users WHERE email = 'olga.psychologist@example.com' LIMIT 1),
    'PSY Studio',
    'Private practice focused on counseling and therapy.',
    '+13125550999',
    'hello@olgapsychology.example',
    'https://olgapsychology.example',
    '14 Lake Street, Chicago, IL',
    TRUE
  ),
  (
    (SELECT id FROM users WHERE email = 'roman.coach@example.com' LIMIT 1),
    'Lider Coaching Hub',
    'Career and leadership coaching for professionals.',
    '+12065550999',
    'contact@romancoaching.example',
    'https://romancoaching.example',
    '205 Pine Ave, Seattle, WA',
    TRUE
  );

INSERT INTO services (business_id, name, description, duration_minutes, price_amount, is_active)
VALUES
  ((SELECT id FROM businesses WHERE name = 'PSY Studio' LIMIT 1), 'Psychology Consultation', 'Initial consultation with a licensed psychologist.', 60, 95.00, TRUE),
  ((SELECT id FROM businesses WHERE name = 'PSY Studio' LIMIT 1), 'Follow-up Therapy Session', 'Follow-up therapeutic session.', 50, 80.00, TRUE),
  ((SELECT id FROM businesses WHERE name = 'Lider Coaching Hub' LIMIT 1), 'Career Strategy Session', 'Career path planning and growth strategy.', 45, 70.00, TRUE),
  ((SELECT id FROM businesses WHERE name = 'Lider Coaching Hub' LIMIT 1), 'Interview Preparation', 'Mock interview with feedback.', 40, 60.00, TRUE),
  ((SELECT id FROM businesses WHERE name = 'Lider Coaching Hub' LIMIT 1), 'Leadership Coaching', 'One-on-one leadership development session.', 75, 120.00, TRUE);

INSERT INTO specialist_services (business_id, specialist_id, service_id, is_active)
VALUES
  (
    (SELECT id FROM businesses WHERE name = 'PSY Studio' LIMIT 1),
    (SELECT id FROM users WHERE email = 'olga.psychologist@example.com' LIMIT 1),
    (SELECT id FROM services WHERE name = 'Psychology Consultation' LIMIT 1),
    TRUE
  ),
  (
    (SELECT id FROM businesses WHERE name = 'PSY Studio' LIMIT 1),
    (SELECT id FROM users WHERE email = 'olga.psychologist@example.com' LIMIT 1),
    (SELECT id FROM services WHERE name = 'Follow-up Therapy Session' LIMIT 1),
    TRUE
  ),
  (
    (SELECT id FROM businesses WHERE name = 'Lider Coaching Hub' LIMIT 1),
    (SELECT id FROM users WHERE email = 'roman.coach@example.com' LIMIT 1),
    (SELECT id FROM services WHERE name = 'Career Strategy Session' LIMIT 1),
    TRUE
  ),
  (
    (SELECT id FROM businesses WHERE name = 'Lider Coaching Hub' LIMIT 1),
    (SELECT id FROM users WHERE email = 'roman.coach@example.com' LIMIT 1),
    (SELECT id FROM services WHERE name = 'Interview Preparation' LIMIT 1),
    TRUE
  ),
  (
    (SELECT id FROM businesses WHERE name = 'Lider Coaching Hub' LIMIT 1),
    (SELECT id FROM users WHERE email = 'roman.coach@example.com' LIMIT 1),
    (SELECT id FROM services WHERE name = 'Leadership Coaching' LIMIT 1),
    TRUE
  );

INSERT INTO appointments (client_id, specialist_service_id, start_time, end_time, status, payment_method, price_amount)
VALUES
  (
    (SELECT id FROM users WHERE email = 'dmitry.sokolov@example.com' LIMIT 1),
    (
      SELECT ss.id
      FROM specialist_services ss
      JOIN users u ON u.id = ss.specialist_id
      JOIN services s ON s.id = ss.service_id
      WHERE u.email = 'olga.psychologist@example.com'
        AND s.name = 'Psychology Consultation'
      LIMIT 1
    ),
    TIMESTAMPTZ '2026-03-02 09:00:00+03',
    TIMESTAMPTZ '2026-03-02 10:00:00+03',
    'PENDING',
    'ONLINE',
    95.00
  ),
  (
    (SELECT id FROM users WHERE email = 'anna.petrenko@example.com' LIMIT 1),
    (
      SELECT ss.id
      FROM specialist_services ss
      JOIN users u ON u.id = ss.specialist_id
      JOIN services s ON s.id = ss.service_id
      WHERE u.email = 'roman.coach@example.com'
        AND s.name = 'Career Strategy Session'
      LIMIT 1
    ),
    TIMESTAMPTZ '2026-03-03 14:00:00+03',
    TIMESTAMPTZ '2026-03-03 14:45:00+03',
    'CONFIRMED',
    'ON_SITE',
    70.00
  ),
  (
    (SELECT id FROM users WHERE email = 'maria.client@example.com' LIMIT 1),
    (
      SELECT ss.id
      FROM specialist_services ss
      JOIN users u ON u.id = ss.specialist_id
      JOIN services s ON s.id = ss.service_id
      WHERE u.email = 'roman.coach@example.com'
        AND s.name = 'Interview Preparation'
      LIMIT 1
    ),
    TIMESTAMPTZ '2026-03-04 12:00:00+03',
    TIMESTAMPTZ '2026-03-04 12:40:00+03',
    'COMPLETED',
    'ONLINE',
    60.00
  ),
  (
    (SELECT id FROM users WHERE email = 'dmitry.sokolov@example.com' LIMIT 1),
    (
      SELECT ss.id
      FROM specialist_services ss
      JOIN users u ON u.id = ss.specialist_id
      JOIN services s ON s.id = ss.service_id
      WHERE u.email = 'olga.psychologist@example.com'
        AND s.name = 'Follow-up Therapy Session'
      LIMIT 1
    ),
    TIMESTAMPTZ '2026-03-06 18:00:00+03',
    TIMESTAMPTZ '2026-03-06 18:50:00+03',
    'CANCELLED',
    'ONLINE',
    80.00
  );

INSERT INTO payments (appointment_id, amount, currency, status)
VALUES
  (
    (
      SELECT a.id
      FROM appointments a
      JOIN users c ON c.id = a.client_id
      JOIN specialist_services ss ON ss.id = a.specialist_service_id
      JOIN services s ON s.id = ss.service_id
      WHERE c.email = 'dmitry.sokolov@example.com'
        AND s.name = 'Psychology Consultation'
      LIMIT 1
    ),
    95.00,
    'EUR',
    'PENDING'
  ),
  (
    (
      SELECT a.id
      FROM appointments a
      JOIN users c ON c.id = a.client_id
      JOIN specialist_services ss ON ss.id = a.specialist_service_id
      JOIN services s ON s.id = ss.service_id
      WHERE c.email = 'maria.client@example.com'
        AND s.name = 'Interview Preparation'
      LIMIT 1
    ),
    60.00,
    'EUR',
    'PAID'
  );
