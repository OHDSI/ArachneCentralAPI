DO
$body$
DECLARE
  default_org_id BIGINT;
BEGIN
  SELECT id INTO default_org_id FROM organizations WHERE name = 'OHDSI';

  IF default_org_id IS NULL THEN
    INSERT INTO organizations (name) VALUES ('OHDSI') RETURNING id INTO default_org_id;
  END IF;

  UPDATE datanodes SET organization_id = default_org_id WHERE organization_id IS NULL;
END;
$body$
LANGUAGE 'plpgsql';

ALTER TABLE datanodes DROP CONSTRAINT datanodes_not_blank_fields_if_published;
ALTER TABLE datanodes ADD CONSTRAINT datanodes_not_blank_fields_if_published
CHECK (
  published = FALSE OR is_virtual = TRUE OR
  (COALESCE(name, '') <> '' AND COALESCE(description, '') <> '' AND organization_id IS NOT NULL)
);