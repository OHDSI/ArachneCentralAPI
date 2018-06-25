-- clean users with duplicated emails/username before - see ARACHNE-2456

ALTER TABLE users_data DROP CONSTRAINT IF EXISTS users_email_unique;
CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique ON users_data (lower(email));

ALTER TABLE users_data DROP CONSTRAINT IF EXISTS users_origin_username_unique;
CREATE UNIQUE INDEX IF NOT EXISTS users_origin_username_unique ON users_data (origin, lower(username));
