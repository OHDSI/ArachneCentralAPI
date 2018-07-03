ALTER TABLE studies_data
  ADD COLUMN IF NOT EXISTS kind VARCHAR NOT NULL DEFAULT 'REGULAR';

ALTER TABLE studies_data
  DROP CONSTRAINT IF EXISTS title_uk;
DROP INDEX IF EXISTS title_uk;

DROP INDEX IF EXISTS studies_data_unique_title_if_kind_is_regular;

CREATE UNIQUE INDEX IF NOT EXISTS studies_data_unique_title_if_kind_is_regular
  ON studies_data (title, tenant_id)
  WHERE (kind = 'REGULAR');

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
    RAISE EXCEPTION 'Workspace for the user id = % in tenant id = % already exists', error_user_id, error_tenant_id;
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
    RAISE EXCEPTION 'Workspace can''t have papers';
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
DECLARE user_role      VARCHAR;
        ws_users_count INTEGER;
BEGIN
  SELECT
    NEW.role,
    COUNT(*)
  into user_role, ws_users_count
  FROM studies_users
    JOIN studies_data ON studies_users.study_id = studies_data.id
  WHERE kind = 'WORKSPACE' AND study_id = NEW.study_id;
  IF (ws_users_count > 1)
  THEN
    RAISE EXCEPTION 'There can be only one user in workspace';
  END IF;
  IF (user_role = 'CONTRIBUTOR')
  THEN
    RAISE EXCEPTION 'Only LEAD_INVESTIGATOR user role is allowed in workspace';
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

-- Recreates view with original definition to update "studies_data" reference

CREATE OR REPLACE VIEW studies AS
  SELECT *
  FROM studies_data
  WHERE tenant_id = current_setting('app.tenant_id')::BIGINT;

-- Recreates view-with-original-definition dependent on "studies" view

CREATE OR REPLACE VIEW studies_view AS WITH study_x_user AS (
    SELECT
      s.id AS study_id,
      u.id AS user_id
    FROM studies s,
      users u
    WHERE ((u.enabled IS TRUE) AND (u.email_confirmed IS TRUE) AND (s.privacy IS FALSE))
), favourites AS (
    SELECT
      favourite_studies.study_id,
      favourite_studies.user_id,
      TRUE AS favourite
    FROM favourite_studies
), all_studies AS (
  SELECT
    su.study_id,
    su.user_id
  FROM study_x_user su
  UNION
  SELECT
    usll.study_id,
    usll.user_id
  FROM users_studies_grouped usll
)
SELECT
  row_number()
  OVER ()       AS id,
  s.study_id,
  s.user_id,
  fv.favourite,
  usll.roles,
  leads.user_id AS first_lead_id
FROM (((all_studies s
  INNER JOIN studies ts ON ts.id = s.study_id
  LEFT JOIN users_studies_grouped usll ON (((s.study_id = usll.study_id) AND (s.user_id = usll.user_id))))
  LEFT JOIN studies_leads leads ON (((leads.study_id = s.study_id) AND (leads.row_number = 1))))
  LEFT JOIN favourites fv ON (((s.user_id = fv.user_id) AND (s.study_id = fv.study_id))));
