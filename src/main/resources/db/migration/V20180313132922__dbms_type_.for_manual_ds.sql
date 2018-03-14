ALTER TABLE data_sources_data
ADD COLUMN dbms_type VARCHAR;

-- recreate
CREATE OR REPLACE VIEW data_sources AS
SELECT *
FROM data_sources_data ds
WHERE EXISTS(
  SELECT 1
  FROM tenants_data_sources tds
  WHERE tds.data_source_id = ds.id AND tds.tenant_id = current_setting('app.tenant_id')::BIGINT
);