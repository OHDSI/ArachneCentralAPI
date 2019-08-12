ALTER TABLE users_data ADD COLUMN IF NOT EXISTS token VARCHAR;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

UPDATE users_data SET token=uuid_generate_v4();