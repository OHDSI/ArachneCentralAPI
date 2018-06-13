CREATE OR REPLACE FUNCTION single_workspace_per_user_in_tenant()
  RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  existing_records INTEGER;
  error_tenant_id  INTEGER;
  error_user_id    INTEGER;
BEGIN
  SELECT
    COUNT(*),
    tenant_id,
    user_id
  INTO existing_records, error_tenant_id, error_user_id
  FROM studies_data d
    JOIN studies_users u ON d.id = u.study_id
  WHERE d.kind = 'WORKSPACE'
  GROUP BY tenant_id, user_id
  HAVING COUNT(*) > 1
  LIMIT 1;

  IF (existing_records > 1)
  THEN
    RAISE EXCEPTION 'Workspace for the user with id = % in tenant with id = % already exists', error_user_id, error_tenant_id;
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS single_study_with_type_workspace
ON studies_data;

CREATE TRIGGER single_study_with_type_workspace
  AFTER INSERT OR UPDATE
  ON studies_data
  FOR EACH ROW EXECUTE PROCEDURE single_workspace_per_user_in_tenant();

DROP TRIGGER IF EXISTS single_lead_user_in_workspace
ON studies_users;

CREATE TRIGGER single_lead_user_in_workspace
  AFTER INSERT OR UPDATE
  ON studies_users
  FOR EACH ROW EXECUTE PROCEDURE single_workspace_per_user_in_tenant();

CREATE OR REPLACE FUNCTION papers_in_workspace_are_prohibited()
  RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  kind_of_study VARCHAR;
BEGIN
  kind_of_study := (SELECT kind
                    FROM studies_data
                      JOIN papers ON studies_data.id = papers.study_id
                    WHERE studies_data.id = NEW.study_id);
  IF (kind_of_study = 'WORKSPACE')
  THEN
    RAISE EXCEPTION 'Workspace with id = % can''t have papers', NEW.study_id;
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS papers_in_workspace_are_prohibited
ON papers;

CREATE TRIGGER papers_in_workspace_are_prohibited
  AFTER INSERT OR UPDATE
  ON papers
  FOR EACH ROW EXECUTE PROCEDURE papers_in_workspace_are_prohibited();

CREATE OR REPLACE FUNCTION lead_user_in_workspace()
  RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE ws_users_count INTEGER;
        ws_user_role   VARCHAR;
BEGIN
  SELECT COUNT(*)
  INTO ws_users_count
  FROM studies_users
    JOIN studies_data ON studies_users.study_id = studies_data.id
  WHERE kind = 'WORKSPACE' AND study_id = NEW.study_id;
  IF (ws_users_count > 1)
  THEN
    RAISE EXCEPTION 'There can be only one user in workspace with id = %', NEW.study_id;
  END IF;
  SELECT role
  INTO ws_user_role
  FROM studies_users
    JOIN studies_data ON studies_users.study_id = studies_data.id
  WHERE kind = 'WORKSPACE' AND study_id = NEW.study_id;
  IF (ws_user_role = 'CONTRIBUTOR')
  THEN
    RAISE EXCEPTION 'Only LEAD_INVESTIGATOR user role is allowed in workspace with id = %', NEW.study_id;
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS lead_user_in_workspace
ON studies_users;

CREATE TRIGGER lead_user_in_workspace
  AFTER INSERT OR UPDATE
  ON studies_users
  FOR EACH ROW EXECUTE PROCEDURE lead_user_in_workspace();