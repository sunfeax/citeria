CREATE TABLE user_avatars (
  user_id UUID PRIMARY KEY,
  data BYTEA NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_avatars_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT user_avatars_content_type_chk CHECK (content_type IN ('image/png', 'image/jpeg')),
  CONSTRAINT user_avatars_size_chk CHECK (octet_length(data) BETWEEN 1 AND 5242880)
);
