ALTER TABLE data_sources_data
    ADD COLUMN IF NOT EXISTS access_type VARCHAR NULL DEFAULT 'PUBLIC';

ALTER TABLE data_sources_data DROP CONSTRAINT data_sources_data_not_blank_fields_if_published;

ALTER TABLE data_sources_data ADD CONSTRAINT data_sources_data_not_blank_fields_if_published
    CHECK (published = FALSE OR (organization <> '' AND model_type <> '' AND access_type <> ''));

DROP VIEW IF EXISTS data_sources;

CREATE OR REPLACE VIEW data_sources
AS
SELECT ds.id,
       ds.name,
       ds.uuid,
       ds.data_node_id,
       ds.model_type,
       ds.health_status,
       ds.health_status_description,
       ds.created,
       ds.deleted,
       ds.cdm_version,
       ds.organization,
       ds.published,
       ds.dbms_type,
       ds.access_type
FROM data_sources_data ds
WHERE EXISTS(
              SELECT 1
              FROM tenants_data_sources tds
              WHERE tds.data_source_id = ds.id AND tds.tenant_id = current_setting('app.tenant_id')::BIGINT
          );

