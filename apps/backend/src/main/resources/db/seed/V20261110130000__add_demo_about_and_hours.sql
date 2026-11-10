-- About texts for the fictional demo specialists and working hours for those
-- that had none. Texts and hours edited by hand are preserved.
UPDATE users AS u SET about = demo.about
FROM (VALUES
  ('22222222-2222-2222-2222-222222222222'::uuid, E'Independent consultant with ten years of experience helping small teams plan their work and ship on time.\n\nSessions are practical: we look at your situation and leave with a short list of next steps.'),
  ('33333333-3333-3333-3333-333333333333'::uuid, E'Coach focused on clear goals and steady habits. I work with people who want structure without pressure.'),
  ('b346bb39-7b81-5716-9ef7-54968df362b1'::uuid, E'Certified personal trainer and former track athlete. I build programs around your schedule, not the other way round.'),
  ('1f50784f-94fb-5e7a-bf6d-170e161c9b66'::uuid, E'Career advisor and former recruiter. I have read thousands of CVs and know what makes a hiring manager keep reading.'),
  ('720147bc-07fe-5488-a382-5a0a0dad25b3'::uuid, E'Yoga and meditation teacher. My classes are slow, friendly and suitable for complete beginners.'),
  ('9fae8b51-d5e4-5d91-879d-0c1f203e712d'::uuid, E'Senior software engineer who has interviewed over two hundred candidates. I help developers prepare for technical interviews and grow their design skills.'),
  ('443b2c15-36b3-520f-97c1-591c8a9d27a5'::uuid, E'Nutritionist who believes healthy food should be simple and enjoyable. Plans come with shopping lists and easy swaps.'),
  ('ce9b7eb8-a861-59fd-b73f-70b086723f9c'::uuid, E'English teacher with a CELTA certificate and eight years of online lessons. I focus on speaking confidence for work and exams.')
) AS demo(id, about)
WHERE u.id = demo.id AND u.about IS NULL;

INSERT INTO working_hours (specialist_id, day_of_week, start_time, end_time)
SELECT hours.specialist_id, hours.day_of_week, hours.start_time::time, hours.end_time::time
FROM (VALUES
  ('b346bb39-7b81-5716-9ef7-54968df362b1'::uuid, 'MONDAY', '07:00', '12:00'),
  ('b346bb39-7b81-5716-9ef7-54968df362b1'::uuid, 'WEDNESDAY', '07:00', '12:00'),
  ('b346bb39-7b81-5716-9ef7-54968df362b1'::uuid, 'FRIDAY', '07:00', '12:00'),
  ('b346bb39-7b81-5716-9ef7-54968df362b1'::uuid, 'SATURDAY', '09:00', '13:00'),
  ('1f50784f-94fb-5e7a-bf6d-170e161c9b66'::uuid, 'TUESDAY', '10:00', '18:00'),
  ('1f50784f-94fb-5e7a-bf6d-170e161c9b66'::uuid, 'THURSDAY', '10:00', '18:00'),
  ('720147bc-07fe-5488-a382-5a0a0dad25b3'::uuid, 'MONDAY', '08:00', '11:00'),
  ('720147bc-07fe-5488-a382-5a0a0dad25b3'::uuid, 'THURSDAY', '17:00', '20:00'),
  ('720147bc-07fe-5488-a382-5a0a0dad25b3'::uuid, 'SUNDAY', '09:00', '12:00'),
  ('9fae8b51-d5e4-5d91-879d-0c1f203e712d'::uuid, 'TUESDAY', '18:00', '21:00'),
  ('9fae8b51-d5e4-5d91-879d-0c1f203e712d'::uuid, 'WEDNESDAY', '18:00', '21:00'),
  ('9fae8b51-d5e4-5d91-879d-0c1f203e712d'::uuid, 'SATURDAY', '10:00', '16:00'),
  ('443b2c15-36b3-520f-97c1-591c8a9d27a5'::uuid, 'MONDAY', '09:00', '15:00'),
  ('443b2c15-36b3-520f-97c1-591c8a9d27a5'::uuid, 'WEDNESDAY', '09:00', '15:00'),
  ('443b2c15-36b3-520f-97c1-591c8a9d27a5'::uuid, 'FRIDAY', '09:00', '15:00'),
  ('ce9b7eb8-a861-59fd-b73f-70b086723f9c'::uuid, 'MONDAY', '09:00', '17:00'),
  ('ce9b7eb8-a861-59fd-b73f-70b086723f9c'::uuid, 'TUESDAY', '09:00', '17:00'),
  ('ce9b7eb8-a861-59fd-b73f-70b086723f9c'::uuid, 'THURSDAY', '12:00', '20:00'),
  ('ce9b7eb8-a861-59fd-b73f-70b086723f9c'::uuid, 'FRIDAY', '09:00', '13:00')
) AS hours(specialist_id, day_of_week, start_time, end_time)
WHERE NOT EXISTS (
  SELECT 1 FROM working_hours existing WHERE existing.specialist_id = hours.specialist_id
);
