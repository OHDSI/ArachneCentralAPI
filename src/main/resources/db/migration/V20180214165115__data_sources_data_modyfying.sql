DROP INDEX IF EXISTS data_sources_name_uk;

CREATE UNIQUE INDEX IF NOT EXISTS data_sources_name_uk
  ON data_sources_data (data_node_id, name)
  WHERE deleted IS NULL;
