CREATE TABLE IF NOT EXISTS atlases_data (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  data_node_id BIGINT REFERENCES datanodes(id),
  version VARCHAR,
  UNIQUE (data_node_id, name)
);

CREATE TABLE IF NOT EXISTS tenants_atlases (
  tenant_id BIGINT REFERENCES tenants(id),
  atlas_id BIGINT REFERENCES atlases_data(id),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  PRIMARY KEY (tenant_id, atlas_id)
);

ALTER TABLE datanodes DROP COLUMN atlas_version;

CREATE OR REPLACE VIEW atlases AS
  SELECT id, name, data_node_id, version
  FROM atlases_data ad
  WHERE EXISTS(
      SELECT 1
      FROM
        tenants_atlases ta
      WHERE ta.atlas_id = ad.id AND ta.tenant_id = current_setting('app.tenant_id')::BIGINT
  );