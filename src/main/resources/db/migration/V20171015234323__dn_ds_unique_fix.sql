ALTER TABLE datanodes DROP CONSTRAINT IF EXISTS name_not_virtual_uq;
DROP INDEX IF EXISTS name_not_virtual_uq;

ALTER TABLE data_sources DROP CONSTRAINT IF EXISTS name_uk;
DROP INDEX IF EXISTS name_uk;

CREATE UNIQUE INDEX IF NOT EXISTS datanodes_not_virtual_name_uq
  ON datanodes (name) WHERE (is_virtual = FALSE);

CREATE UNIQUE INDEX IF NOT EXISTS data_sources_name_uk
  ON data_sources (data_node_id, name);