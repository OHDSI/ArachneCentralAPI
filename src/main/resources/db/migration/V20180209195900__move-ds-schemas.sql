ALTER TABLE data_sources_data
ADD COLUMN published BOOLEAN DEFAULT FALSE NOT NULL;

UPDATE data_sources_data set published = TRUE;

ALTER TABLE data_sources_data ALTER organization DROP NOT NULL;
ALTER TABLE data_sources_data ALTER model_type DROP NOT NULL;

ALTER TABLE datanodes ALTER name DROP NOT NULL;
ALTER TABLE datanodes ALTER description DROP NOT NULL;

ALTER TABLE datanodes
ADD COLUMN published BOOLEAN DEFAULT FALSE NOT NULL;
UPDATE datanodes set published = TRUE WHERE name <> '' AND description <> '';

ALTER TABLE datanodes ADD CONSTRAINT datanodes_not_blank_fields_if_published
CHECK (published = FALSE OR (name <> '' AND description <> ''));

ALTER TABLE data_sources_data ADD CONSTRAINT data_sources_data_not_blank_fields_if_published
CHECK (published = FALSE OR (organization <> '' AND model_type <> ''));

create or replace function checkParentDataNode(ds_id BIGINT)
  returns BOOLEAN AS $$
begin
return
(select dn.published
from data_sources_data ds JOIN datanodes dn on ds.data_node_id = dn.id
where ds.id = ds_id);
end;
$$ LANGUAGE plpgsql;

ALTER TABLE data_sources_data ADD CONSTRAINT publish_datasource_only_for_published_datanode
CHECK (published = FALSE OR checkParentDataNode(id));

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
