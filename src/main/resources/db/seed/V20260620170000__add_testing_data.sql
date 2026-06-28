INSERT INTO users (id, first_name, last_name, email, phone, password, role, type) VALUES
  ('11111111-1111-1111-1111-111111111111', 'Admin', 'User', 'admin@citeria.test', '+3725550000', '$2a$10$cFvDTaG3fmCcVdmOblILyO9jXnoXWjJ.LfIfu.kxaMxssE5tJdV8G', 'ADMIN', 'CLIENT'),
  ('22222222-2222-2222-2222-222222222222', 'Anna', 'Specialist', 'anna@citeria.test', '+3725550001', '$2a$10$cFvDTaG3fmCcVdmOblILyO9jXnoXWjJ.LfIfu.kxaMxssE5tJdV8G', 'USER', 'SPECIALIST'),
  ('33333333-3333-3333-3333-333333333333', 'Boris', 'Coach', 'boris@citeria.test', '+3725550002', '$2a$10$cFvDTaG3fmCcVdmOblILyO9jXnoXWjJ.LfIfu.kxaMxssE5tJdV8G', 'USER', 'SPECIALIST'),
  ('44444444-4444-4444-4444-444444444444', 'Clara', 'Client', 'clara@citeria.test', '+3725550003', '$2a$10$cFvDTaG3fmCcVdmOblILyO9jXnoXWjJ.LfIfu.kxaMxssE5tJdV8G', 'USER', 'CLIENT'),
  ('55555555-5555-5555-5555-555555555555', 'Dan', 'Client', 'dan@citeria.test', '+3725550004', '$2a$10$cFvDTaG3fmCcVdmOblILyO9jXnoXWjJ.LfIfu.kxaMxssE5tJdV8G', 'USER', 'CLIENT');

INSERT INTO services (id, specialist_id, name, description, duration_minutes, price_amount, currency) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '22222222-2222-2222-2222-222222222222', 'Consultation', 'Initial consultation', 60, 60.00, 'EUR'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '22222222-2222-2222-2222-222222222222', 'Deep Session', 'Extended therapy session', 90, 90.00, 'EUR'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', '33333333-3333-3333-3333-333333333333', 'Coaching', 'One-on-one coaching', 30, 40.00, 'EUR');

INSERT INTO working_hours (specialist_id, day_of_week, start_time, end_time) VALUES
  ('22222222-2222-2222-2222-222222222222', 'MONDAY', '09:00', '17:00'),
  ('22222222-2222-2222-2222-222222222222', 'TUESDAY', '09:00', '17:00'),
  ('22222222-2222-2222-2222-222222222222', 'WEDNESDAY', '09:00', '17:00'),
  ('22222222-2222-2222-2222-222222222222', 'THURSDAY', '09:00', '17:00'),
  ('22222222-2222-2222-2222-222222222222', 'FRIDAY', '09:00', '17:00'),
  ('33333333-3333-3333-3333-333333333333', 'MONDAY', '10:00', '14:00'),
  ('33333333-3333-3333-3333-333333333333', 'WEDNESDAY', '10:00', '14:00'),
  ('33333333-3333-3333-3333-333333333333', 'FRIDAY', '10:00', '14:00');
