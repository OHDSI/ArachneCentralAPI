CREATE TABLE IF NOT EXISTS tenants (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL UNIQUE
);

INSERT INTO tenants (name) VALUES ('Everyone');

-- Users

CREATE TABLE IF NOT EXISTS tenants_users (
  tenant_id BIGINT REFERENCES tenants(id),
  user_id BIGINT REFERENCES users(id),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  PRIMARY KEY (tenant_id, user_id)
);

INSERT INTO tenants_users (tenant_id, user_id)
SELECT t.id, u.id
FROM tenants t, users u;

ALTER TABLE users
ADD COLUMN IF NOT EXISTS active_tenant_id BIGINT
REFERENCES tenants(id);

UPDATE users
SET active_tenant_id = (SELECT id FROM tenants LIMIT 1);

-- Studies

ALTER TABLE studies
ADD COLUMN IF NOT EXISTS tenant_id BIGINT
REFERENCES tenants(id);

UPDATE studies
SET tenant_id = (SELECT id FROM tenants LIMIT 1);

ALTER TABLE studies ALTER COLUMN tenant_id SET NOT NULL;

-- Data sources

CREATE TABLE IF NOT EXISTS tenants_data_sources (
  tenant_id BIGINT REFERENCES tenants(id),
  data_source_id BIGINT REFERENCES data_sources(id),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  PRIMARY KEY (tenant_id, data_source_id)
);

INSERT INTO tenants_data_sources (tenant_id, data_source_id)
SELECT t.id, ds.id
FROM tenants t, data_sources ds;