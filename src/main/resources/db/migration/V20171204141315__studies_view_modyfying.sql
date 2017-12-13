DROP VIEW IF EXISTS studies_view;

CREATE VIEW studies_view AS WITH study_x_user AS (
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
  LEFT JOIN users_studies_grouped usll ON (((s.study_id = usll.study_id) AND (s.user_id = usll.user_id))))
  LEFT JOIN studies_leads leads ON (((leads.study_id = s.study_id) AND (leads.row_number = 1))))
  LEFT JOIN favourites fv ON (((s.user_id = fv.user_id) AND (s.study_id = fv.study_id))));
