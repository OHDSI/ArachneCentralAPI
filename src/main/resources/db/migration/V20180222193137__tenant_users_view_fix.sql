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
)
SELECT DISTINCT ON (tenant_id, user_id) *
FROM all_tenant_users;