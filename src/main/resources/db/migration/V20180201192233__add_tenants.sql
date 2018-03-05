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

-- Studies

ALTER TABLE studies
ADD COLUMN IF NOT EXISTS tenant_id BIGINT
REFERENCES tenants(id);

UPDATE studies
SET tenant_id = (SELECT id FROM tenants LIMIT 1);

ALTER TABLE studies ALTER COLUMN tenant_id SET NOT NULL;

-- Although, this is not the prettiest looking solution, changing all places, where the table is used (inc. cascade views) looks for me even worse

-- Studies

ALTER TABLE studies RENAME TO studies_data;

-- Since the view is simple, it is automatically becomes updatable one
CREATE OR REPLACE VIEW studies AS
SELECT *
FROM studies_data
WHERE tenant_id = current_setting('app.tenant_id')::BIGINT;


-- 1) Dependent views reference the "studies" table by OID, not table name,
-- so after table renaming, they will continue to reference old table.
-- Therefore, there is a need to re-create views to force them reference new OIDs
-- https://dba.stackexchange.com/questions/87220/postgresql-rename-table-without-updating-view-definitions
-- 2) Need to limit results w/ tenant studies
CREATE OR REPLACE VIEW studies_view AS WITH study_x_user AS (
    SELECT
      s.id AS study_id,
      u.id AS user_id
    FROM studies s,
      users u
    WHERE ((u.enabled IS TRUE) AND (u.email_confirmed IS TRUE) AND (s.privacy IS FALSE))
), favourites AS (
    SELECT
      favourite_studies.study_id,
      favourite_studies.user_id,
      TRUE AS favourite
    FROM favourite_studies
), all_studies AS (
  SELECT
    su.study_id,
    su.user_id
  FROM study_x_user su
  UNION
  SELECT
    usll.study_id,
    usll.user_id
  FROM users_studies_grouped usll
)
SELECT
  row_number()
  OVER ()       AS id,
  s.study_id,
  s.user_id,
  fv.favourite,
  usll.roles,
  leads.user_id AS first_lead_id
FROM (((all_studies s
  INNER JOIN studies ts ON ts.id = s.study_id
  LEFT JOIN users_studies_grouped usll ON (((s.study_id = usll.study_id) AND (s.user_id = usll.user_id))))
  LEFT JOIN studies_leads leads ON (((leads.study_id = s.study_id) AND (leads.row_number = 1))))
  LEFT JOIN favourites fv ON (((s.user_id = fv.user_id) AND (s.study_id = fv.study_id))));

-- Data sources

ALTER TABLE data_sources RENAME TO data_sources_data;

CREATE OR REPLACE VIEW data_sources AS
SELECT *
FROM data_sources_data ds
WHERE EXISTS(
  SELECT 1
  FROM tenants_data_sources tds
  WHERE tds.data_source_id = ds.id AND tds.tenant_id = current_setting('app.tenant_id')::BIGINT
);

-- Users

ALTER TABLE users RENAME TO users_data;

-- Includes calculated (dependent) users of tenants: adding DS owners of to tenant, if the DS was attached to the tenant

CREATE VIEW tenant_dependent_users_view AS
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

CREATE OR REPLACE VIEW users AS
SELECT *
FROM users_data u
WHERE EXISTS(
  SELECT 1
  FROM tenant_dependent_users_view tu
  WHERE tu.user_id = u.id AND tu.tenant_id = current_setting('app.tenant_id')::BIGINT
);