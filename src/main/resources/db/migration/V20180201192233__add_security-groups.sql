CREATE TABLE IF NOT EXISTS security_groups (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL
);

INSERT INTO security_groups (name) VALUES ('Everyone');

-- Users

CREATE TABLE IF NOT EXISTS security_groups_users (
  security_group_id BIGINT REFERENCES security_groups(id),
  user_id BIGINT REFERENCES users(id),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  PRIMARY KEY (security_group_id, user_id)
);

INSERT INTO security_groups_users (security_group_id, user_id)
SELECT sg.id, u.id
FROM security_groups sg, users u;

ALTER TABLE users
ADD COLUMN IF NOT EXISTS active_security_group_id BIGINT
REFERENCES security_groups(id);

UPDATE users
SET active_security_group_id = (SELECT id FROM security_groups LIMIT 1);

-- Studies

ALTER TABLE studies
ADD COLUMN IF NOT EXISTS security_group_id BIGINT
REFERENCES security_groups(id);

UPDATE studies
SET security_group_id = (SELECT id FROM security_groups LIMIT 1);

ALTER TABLE studies ALTER COLUMN security_group_id SET NOT NULL;

-- Data sources

CREATE TABLE IF NOT EXISTS security_groups_data_sources (
  security_group_id BIGINT REFERENCES security_groups(id),
  data_source_id BIGINT REFERENCES data_sources(id),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  PRIMARY KEY (security_group_id, data_source_id)
);

INSERT INTO security_groups_data_sources (security_group_id, data_source_id)
SELECT sg.id, ds.id
FROM security_groups sg, data_sources ds;