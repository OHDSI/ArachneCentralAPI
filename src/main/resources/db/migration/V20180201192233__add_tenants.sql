CREATE TABLE IF NOT EXISTS tenants (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL UNIQUE
);

INSERT INTO tenants (name) VALUES ('Everyone');

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

CREATE VIEW tenant_dependant_users_view AS
WITH all_tenant_users AS (
  SELECT *
  FROM tenants_users
  UNION
  SELECT
    tds.tenant_id,
    dnu.user_id,
    tds.created_at
  FROM
    tenants_data_sources tds
    JOIN data_sources_data ds ON ds.id = tds.data_source_id
    JOIN datanodes dn ON dn.id = ds.data_node_id
    JOIN datanodes_users dnu ON dnu.datanode_id = dn.id
    JOIN datanodes_users_roles dnur ON dnur.datanode_user_id = dnu.id AND dnur.datanode_role = 'ADMIN'
)
SELECT DISTINCT ON (tenant_id, user_id) *
FROM all_tenant_users;

-- Studies

ALTER TABLE studies
ADD COLUMN IF NOT EXISTS tenant_id BIGINT
REFERENCES tenants(id);

UPDATE studies
SET tenant_id = (SELECT id FROM tenants LIMIT 1);

ALTER TABLE studies ALTER COLUMN tenant_id SET NOT NULL;