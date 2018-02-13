ALTER TABLE data_sources_data
ADD COLUMN published BOOLEAN DEFAULT TRUE NOT NULL;

ALTER TABLE data_sources_data ALTER organization DROP NOT NULL;
ALTER TABLE data_sources_data ALTER model_type DROP NOT NULL;

ALTER TABLE datanodes ALTER name DROP NOT NULL;
ALTER TABLE datanodes ALTER description DROP NOT NULL;

--just recreating the views

CREATE OR REPLACE VIEW data_sources AS
SELECT *
FROM data_sources_data ds
WHERE EXISTS(
  SELECT 1
  FROM tenants_data_sources tds
  WHERE tds.data_source_id = ds.id AND tds.tenant_id = current_setting('app.tenant_id')::BIGINT
);

CREATE OR REPLACE VIEW tenant_dependent_users_view AS
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