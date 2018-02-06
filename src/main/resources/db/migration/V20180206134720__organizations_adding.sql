CREATE SEQUENCE IF NOT EXISTS organizations_id_seq
  MINVALUE 1;
CREATE TABLE IF NOT EXISTS organizations (
  id   BIGINT PRIMARY KEY NOT NULL DEFAULT nextval('organizations_id_seq'),
  name VARCHAR            NOT NULL UNIQUE
);

ALTER TABLE datanodes
  ADD COLUMN organization_id BIGINT REFERENCES organizations (id);
