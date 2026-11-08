ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

ALTER TABLE users ADD COLUMN google_id VARCHAR(255);

ALTER TABLE users ADD CONSTRAINT users_google_id_key UNIQUE (google_id);
