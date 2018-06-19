/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: September 28, 2017
 *
 */

CREATE SEQUENCE IF NOT EXISTS achilles_files_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS achilles_permissions_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS achilles_report_matchers_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS achilles_reports_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS analyses_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS analyses_files_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS analysis_unlock_requests_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS characterizations_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS comment_topics_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS comments_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS countries_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS data_references_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS data_source_health_check_journal_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS data_sources_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS datanodes_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS datanodes_health_check_journal_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS datanodes_journal_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS datanodes_statuses_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS datanodes_users_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS datanodes_users_roles_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS favourite_studies_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS mimetype_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS paper_favourites_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS paper_papers_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS paper_protocols_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS papers_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS password_resets_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS private_repositories_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS professional_types_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS result_files_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS roles_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS schema_version_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS skills_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS states_provinces_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS studies_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS studies_data_sources_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS studies_data_sources_comment_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS studies_files_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS studies_users_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS study_statuses_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS study_types_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS submission_files_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS submission_groups_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS submission_insight_submission_files_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS submission_insights_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS submission_status_history_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS submissions_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS system_settings_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS system_settings_groups_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS user_links_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS user_publications_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS user_registrants_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS users_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS users_roles_id_seq MINVALUE 1;
CREATE SEQUENCE IF NOT EXISTS users_skills_id_seq MINVALUE 1;

CREATE TABLE IF NOT EXISTS achilles_files
(
  id                  BIGINT PRIMARY KEY NOT NULL,
  characterization_id BIGINT,
  file_path           VARCHAR            NOT NULL,
  data                JSONB
);

ALTER TABLE achilles_files ALTER COLUMN id SET DEFAULT nextval('achilles_files_id_seq');
ALTER SEQUENCE achilles_files_id_seq OWNED BY achilles_files.id;
select setval('achilles_files_id_seq', (SELECT MAX(id)+1 FROM achilles_files), false);


CREATE TABLE IF NOT EXISTS achilles_permissions
(
  id                 BIGINT PRIMARY KEY NOT NULL,
  datasource_id      BIGINT             NOT NULL,
  achilles_report_id BIGINT             NOT NULL,
  grant_type         VARCHAR            NOT NULL
);

ALTER TABLE achilles_permissions ALTER COLUMN id SET DEFAULT nextval('achilles_permissions_id_seq');
ALTER SEQUENCE achilles_permissions_id_seq OWNED BY achilles_permissions.id;
select setval('achilles_permissions_id_seq', (SELECT MAX(id)+1 FROM achilles_permissions), false);


CREATE TABLE IF NOT EXISTS achilles_report_matchers
(
  id                 BIGINT PRIMARY KEY NOT NULL,
  achilles_report_id BIGINT             NOT NULL,
  pattern            VARCHAR            NOT NULL
);

ALTER TABLE achilles_report_matchers ALTER COLUMN id SET DEFAULT nextval('achilles_report_matchers_id_seq');
ALTER SEQUENCE achilles_report_matchers_id_seq OWNED BY achilles_report_matchers.id;
select setval('achilles_report_matchers_id_seq', (SELECT MAX(id)+1 FROM achilles_report_matchers), false);


CREATE TABLE IF NOT EXISTS achilles_reports
(
  id         BIGINT PRIMARY KEY NOT NULL,
  label      VARCHAR            NOT NULL,
  name       VARCHAR            NOT NULL,
  sort_order INTEGER            NOT NULL
);

ALTER TABLE achilles_reports ALTER COLUMN id SET DEFAULT nextval('achilles_reports_id_seq');
ALTER SEQUENCE achilles_reports_id_seq OWNED BY achilles_reports.id;
select setval('achilles_reports_id_seq', (SELECT MAX(id)+1 FROM achilles_reports), false);


CREATE UNIQUE INDEX IF NOT EXISTS achilles_reports_label_key
  ON achilles_reports (label);

CREATE TABLE IF NOT EXISTS analyses
(
  id          BIGINT PRIMARY KEY                     NOT NULL,
  title       VARCHAR(255),
  author_id   BIGINT,
  study_id    BIGINT,
  ord         BIGINT,
  description VARCHAR(1000),
  created     TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  updated     TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  is_locked   BOOLEAN DEFAULT FALSE                  NOT NULL,
  type        VARCHAR                                NOT NULL
);

ALTER TABLE analyses ALTER COLUMN id SET DEFAULT nextval('analyses_id_seq');
ALTER SEQUENCE analyses_id_seq OWNED BY analyses.id;
select setval('analyses_id_seq', (SELECT MAX(id)+1 FROM analyses), false);


CREATE TABLE IF NOT EXISTS analyses_files
(
  id                BIGINT PRIMARY KEY    NOT NULL,
  analysis_id       BIGINT                NOT NULL,
  uuid              VARCHAR               NOT NULL,
  real_name         VARCHAR               NOT NULL,
  executable        BOOLEAN DEFAULT FALSE NOT NULL,
  created           TIMESTAMP WITH TIME ZONE,
  updated           TIMESTAMP WITH TIME ZONE,
  mime_type         VARCHAR,
  label             VARCHAR,
  author_id         BIGINT,
  version           INTEGER DEFAULT 1,
  updated_by        BIGINT,
  data_reference_id BIGINT,
  entry_point       VARCHAR,
  content_type      VARCHAR
);

ALTER TABLE analyses_files ALTER COLUMN id SET DEFAULT nextval('analyses_files_id_seq');
ALTER SEQUENCE analyses_files_id_seq OWNED BY analyses_files.id;
select setval('analyses_files_id_seq', (SELECT MAX(id)+1 FROM analyses_files), false);


CREATE UNIQUE INDEX IF NOT EXISTS analysis_file_name_uk
  ON analyses_files (uuid);

CREATE TABLE IF NOT EXISTS analysis_unlock_requests
(
  id          BIGINT                   NOT NULL,
  user_id     BIGINT                   NOT NULL,
  analysis_id BIGINT                   NOT NULL,
  created     TIMESTAMP WITH TIME ZONE NOT NULL,
  description VARCHAR                  NOT NULL,
  status      VARCHAR                  NOT NULL,
  token       VARCHAR                  NOT NULL
);

ALTER TABLE analysis_unlock_requests ALTER COLUMN id SET DEFAULT nextval('analysis_unlock_requests_id_seq');
ALTER SEQUENCE analysis_unlock_requests_id_seq OWNED BY analysis_unlock_requests.id;
select setval('analysis_unlock_requests_id_seq', (SELECT MAX(id)+1 FROM analysis_unlock_requests), false);


CREATE UNIQUE INDEX IF NOT EXISTS analysis_pending_uq
  ON analysis_unlock_requests (analysis_id, status);

CREATE TABLE IF NOT EXISTS characterizations
(
  id            BIGINT PRIMARY KEY NOT NULL,
  datasource_id BIGINT             NOT NULL,
  date          TIMESTAMP          NOT NULL
);

ALTER TABLE characterizations ALTER COLUMN id SET DEFAULT nextval('characterizations_id_seq');
ALTER SEQUENCE characterizations_id_seq OWNED BY characterizations.id;
select setval('characterizations_id_seq', (SELECT MAX(id)+1 FROM characterizations), false);


CREATE TABLE IF NOT EXISTS comment_topics
(
  id BIGINT PRIMARY KEY NOT NULL
);

ALTER TABLE comment_topics ALTER COLUMN id SET DEFAULT nextval('comment_topics_id_seq');
ALTER SEQUENCE comment_topics_id_seq OWNED BY comment_topics.id;
select setval('comment_topics_id_seq', (SELECT MAX(id)+1 FROM comment_topics), false);


CREATE TABLE IF NOT EXISTS comments
(
  id        BIGINT PRIMARY KEY                     NOT NULL,
  date      TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  comment   VARCHAR                                NOT NULL,
  parent_id BIGINT,
  topic_id  BIGINT,
  author_id BIGINT
);

ALTER TABLE comments ALTER COLUMN id SET DEFAULT nextval('comments_id_seq');
ALTER SEQUENCE comments_id_seq OWNED BY comments.id;
select setval('comments_id_seq', (SELECT MAX(id)+1 FROM comments), false);


CREATE TABLE IF NOT EXISTS countries
(
  id    BIGINT PRIMARY KEY NOT NULL,
  name  VARCHAR(255)       NOT NULL,
  alfa2 VARCHAR(255)       NOT NULL,
  alfa3 VARCHAR(255)       NOT NULL
);

ALTER TABLE countries ALTER COLUMN id SET DEFAULT nextval('countries_id_seq');
ALTER SEQUENCE countries_id_seq OWNED BY countries.id;
select setval('countries_id_seq', (SELECT MAX(id)+1 FROM countries), false);


CREATE TABLE IF NOT EXISTS data_references
(
  id           BIGINT PRIMARY KEY NOT NULL,
  guid         VARCHAR            NOT NULL,
  data_node_id BIGINT
);

ALTER TABLE data_references ALTER COLUMN id SET DEFAULT nextval('data_references_id_seq');
ALTER SEQUENCE data_references_id_seq OWNED BY data_references.id;
select setval('data_references_id_seq', (SELECT MAX(id)+1 FROM data_references), false);


CREATE UNIQUE INDEX IF NOT EXISTS datanode_guid_uk
  ON data_references (guid, data_node_id);

CREATE TABLE IF NOT EXISTS data_source_health_check_journal
(
  id             BIGINT PRIMARY KEY                     NOT NULL,
  created        TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  data_source_id BIGINT,
  delay          BIGINT                                 NOT NULL
);

ALTER TABLE data_source_health_check_journal ALTER COLUMN id SET DEFAULT nextval('data_source_health_check_journal_id_seq');
ALTER SEQUENCE data_source_health_check_journal_id_seq OWNED BY data_source_health_check_journal.id;
select setval('data_source_health_check_journal_id_seq', (SELECT MAX(id)+1 FROM data_source_health_check_journal), false);


CREATE TABLE IF NOT EXISTS data_sources
(
  id                        BIGINT PRIMARY KEY                                   NOT NULL,
  name                      VARCHAR(255)                                         NOT NULL,
  uuid                      VARCHAR(50)                                          NOT NULL,
  data_node_id              BIGINT,
  model_type                VARCHAR                                              NOT NULL,
  health_status             VARCHAR DEFAULT 'NOT_COLLECTED' :: CHARACTER VARYING NOT NULL,
  health_status_description VARCHAR,
  created                   TIMESTAMP WITH TIME ZONE DEFAULT now()               NOT NULL,
  deleted                   TIMESTAMP WITH TIME ZONE
);

ALTER TABLE data_sources ALTER COLUMN id SET DEFAULT nextval('data_sources_id_seq');
ALTER SEQUENCE data_sources_id_seq OWNED BY data_sources.id;
select setval('data_sources_id_seq', (SELECT MAX(id)+1 FROM data_sources), false);


CREATE UNIQUE INDEX IF NOT EXISTS name_uk
  ON data_sources (name);

CREATE UNIQUE INDEX IF NOT EXISTS data_sources_sid_key
  ON data_sources (uuid);

CREATE TABLE IF NOT EXISTS datanodes
(
  id                        BIGINT PRIMARY KEY                                   NOT NULL,
  sid                       VARCHAR(50)                                          NOT NULL,
  status_id                 BIGINT                                               NOT NULL,
  health_status             VARCHAR DEFAULT 'NOT_COLLECTED' :: CHARACTER VARYING NOT NULL,
  health_status_description VARCHAR,
  name                      VARCHAR                                              NOT NULL,
  description               VARCHAR                                              NOT NULL,
  token                     VARCHAR                                              NOT NULL,
  created                   TIMESTAMP WITH TIME ZONE DEFAULT now()               NOT NULL,
  is_virtual                BOOLEAN                                              NOT NULL,
  atlas_version             VARCHAR
);

ALTER TABLE datanodes ALTER COLUMN id SET DEFAULT nextval('datanodes_id_seq');
ALTER SEQUENCE datanodes_id_seq OWNED BY datanodes.id;
select setval('datanodes_id_seq', (SELECT MAX(id)+1 FROM datanodes), false);


CREATE UNIQUE INDEX IF NOT EXISTS datanodes_sid_key
  ON datanodes (sid);

CREATE UNIQUE INDEX IF NOT EXISTS name_not_virtual_uq
  ON datanodes (name, is_virtual);

CREATE TABLE IF NOT EXISTS datanodes_health_check_journal
(
  id           BIGINT PRIMARY KEY      NOT NULL,
  created      TIMESTAMP DEFAULT now() NOT NULL,
  data_node_id BIGINT
);

ALTER TABLE datanodes_health_check_journal ALTER COLUMN id SET DEFAULT nextval('datanodes_health_check_journal_id_seq');
ALTER SEQUENCE datanodes_health_check_journal_id_seq OWNED BY datanodes_health_check_journal.id;
select setval('datanodes_health_check_journal_id_seq', (SELECT MAX(id)+1 FROM datanodes_health_check_journal), false);


CREATE TABLE IF NOT EXISTS datanodes_journal
(
  id            BIGINT PRIMARY KEY NOT NULL,
  timestamp     TIMESTAMP          NOT NULL,
  old_status_id BIGINT             NOT NULL,
  new_status_id BIGINT             NOT NULL,
  datanode_id   BIGINT             NOT NULL,
  message       VARCHAR(1000)
);

ALTER TABLE datanodes_journal ALTER COLUMN id SET DEFAULT nextval('datanodes_journal_id_seq');
ALTER SEQUENCE datanodes_journal_id_seq OWNED BY datanodes_journal.id;
select setval('datanodes_journal_id_seq', (SELECT MAX(id)+1 FROM datanodes_journal), false);


CREATE TABLE IF NOT EXISTS datanodes_statuses
(
  id   BIGINT PRIMARY KEY NOT NULL,
  name VARCHAR(255)       NOT NULL
);

ALTER TABLE datanodes_statuses ALTER COLUMN id SET DEFAULT nextval('datanodes_statuses_id_seq');
ALTER SEQUENCE datanodes_statuses_id_seq OWNED BY datanodes_statuses.id;
select setval('datanodes_statuses_id_seq', (SELECT MAX(id)+1 FROM datanodes_statuses), false);


CREATE UNIQUE INDEX IF NOT EXISTS datanodes_statuses_name_key
  ON datanodes_statuses (name);

CREATE TABLE IF NOT EXISTS datanodes_users
(
  datanode_id BIGINT             NOT NULL,
  user_id     BIGINT             NOT NULL,
  id          BIGINT PRIMARY KEY NOT NULL
);

ALTER TABLE datanodes_users ALTER COLUMN id SET DEFAULT nextval('datanodes_users_id_seq');
ALTER SEQUENCE datanodes_users_id_seq OWNED BY datanodes_users.id;
select setval('datanodes_users_id_seq', (SELECT MAX(id)+1 FROM datanodes_users), false);


CREATE UNIQUE INDEX IF NOT EXISTS datanode_user_uq
  ON datanodes_users (datanode_id, user_id);

CREATE TABLE IF NOT EXISTS datanodes_users_roles
(
  datanode_user_id BIGINT,
  datanode_role    VARCHAR DEFAULT 'USER' :: CHARACTER VARYING NOT NULL
);

CREATE TABLE IF NOT EXISTS favourite_studies
(
  user_id  BIGINT NOT NULL,
  study_id BIGINT NOT NULL,
  CONSTRAINT favourite_studies_pkey PRIMARY KEY (user_id, study_id)
);

CREATE TABLE IF NOT EXISTS mimetype
(
  id           BIGINT PRIMARY KEY NOT NULL,
  content_type VARCHAR            NOT NULL,
  app          VARCHAR            NOT NULL,
  ext          VARCHAR            NOT NULL
);

ALTER TABLE mimetype ALTER COLUMN id SET DEFAULT nextval('mimetype_id_seq');
ALTER SEQUENCE mimetype_id_seq OWNED BY mimetype.id;
select setval('mimetype_id_seq', (SELECT MAX(id)+1 FROM mimetype), false);

CREATE TABLE IF NOT EXISTS paper_favourites
(
  user_id  BIGINT,
  paper_id BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS user_paper_uk
  ON paper_favourites (user_id, paper_id);

CREATE TABLE IF NOT EXISTS paper_papers
(
  id           BIGINT PRIMARY KEY       NOT NULL,
  paper_id     BIGINT                   NOT NULL,
  uuid         VARCHAR                  NOT NULL,
  real_name    VARCHAR                  NOT NULL,
  created      TIMESTAMP WITH TIME ZONE NOT NULL,
  updated      TIMESTAMP WITH TIME ZONE,
  mime_type    VARCHAR,
  label        VARCHAR,
  link         VARCHAR(2000),
  author_id    BIGINT                   NOT NULL,
  content_type VARCHAR
);

ALTER TABLE paper_papers ALTER COLUMN id SET DEFAULT nextval('paper_papers_id_seq');
ALTER SEQUENCE paper_papers_id_seq OWNED BY paper_papers.id;
select setval('paper_papers_id_seq', (SELECT MAX(id)+1 FROM paper_papers), false);


CREATE UNIQUE INDEX IF NOT EXISTS paper_papers_uuid_key
  ON paper_papers (uuid);

CREATE TABLE IF NOT EXISTS paper_protocols
(
  id           BIGINT PRIMARY KEY       NOT NULL,
  paper_id     BIGINT                   NOT NULL,
  uuid         VARCHAR                  NOT NULL,
  real_name    VARCHAR                  NOT NULL,
  created      TIMESTAMP WITH TIME ZONE NOT NULL,
  updated      TIMESTAMP WITH TIME ZONE,
  mime_type    VARCHAR,
  label        VARCHAR,
  link         VARCHAR(2000),
  author_id    BIGINT                   NOT NULL,
  content_type VARCHAR
);

ALTER TABLE paper_protocols ALTER COLUMN id SET DEFAULT nextval('paper_protocols_id_seq');
ALTER SEQUENCE paper_protocols_id_seq OWNED BY paper_protocols.id;
select setval('paper_protocols_id_seq', (SELECT MAX(id)+1 FROM paper_protocols), false);


CREATE UNIQUE INDEX IF NOT EXISTS paper_protocols_uuid_key
  ON paper_protocols (uuid);

CREATE TABLE IF NOT EXISTS papers
(
  id             BIGINT PRIMARY KEY                           NOT NULL,
  study_id       BIGINT                                       NOT NULL,
  publish_state  VARCHAR DEFAULT 'DRAFT' :: CHARACTER VARYING NOT NULL,
  published_date TIMESTAMP WITH TIME ZONE
);

ALTER TABLE papers ALTER COLUMN id SET DEFAULT nextval('papers_id_seq');
ALTER SEQUENCE papers_id_seq OWNED BY papers.id;
select setval('papers_id_seq', (SELECT MAX(id)+1 FROM papers), false);


CREATE UNIQUE INDEX IF NOT EXISTS study_id_uq
  ON papers (study_id);

CREATE TABLE IF NOT EXISTS password_resets
(
  id      BIGINT PRIMARY KEY      NOT NULL,
  email   VARCHAR(255)            NOT NULL,
  token   VARCHAR(255)            NOT NULL,
  created TIMESTAMP DEFAULT now() NOT NULL
);

ALTER TABLE password_resets ALTER COLUMN id SET DEFAULT nextval('password_resets_id_seq');
ALTER SEQUENCE password_resets_id_seq OWNED BY password_resets.id;
select setval('password_resets_id_seq', (SELECT MAX(id)+1 FROM password_resets), false);



CREATE TABLE IF NOT EXISTS professional_types
(
  id   BIGINT PRIMARY KEY NOT NULL,
  name VARCHAR(255)
);

ALTER TABLE professional_types ALTER COLUMN id SET DEFAULT nextval('professional_types_id_seq');
ALTER SEQUENCE professional_types_id_seq OWNED BY professional_types.id;
select setval('professional_types_id_seq', (SELECT MAX(id)+1 FROM professional_types), false);


CREATE TABLE IF NOT EXISTS result_files
(
  id               BIGINT PRIMARY KEY NOT NULL,
  submission_id    BIGINT             NOT NULL,
  uuid             VARCHAR            NOT NULL,
  real_name        VARCHAR            NOT NULL,
  created          TIMESTAMP WITH TIME ZONE,
  updated          TIMESTAMP WITH TIME ZONE,
  mime_type        VARCHAR,
  label            VARCHAR,
  comment_topic_id BIGINT,
  manual_upload    BOOLEAN DEFAULT FALSE,
  content_type     VARCHAR
);

ALTER TABLE result_files ALTER COLUMN id SET DEFAULT nextval('result_files_id_seq');
ALTER SEQUENCE result_files_id_seq OWNED BY result_files.id;
select setval('result_files_id_seq', (SELECT MAX(id)+1 FROM result_files), false);


CREATE UNIQUE INDEX IF NOT EXISTS result_file_name_uk
  ON result_files (uuid);

CREATE TABLE IF NOT EXISTS roles
(
  id          BIGINT PRIMARY KEY NOT NULL,
  name        VARCHAR(255),
  description VARCHAR(255)
);

ALTER TABLE roles ALTER COLUMN id SET DEFAULT nextval('roles_id_seq');
ALTER SEQUENCE roles_id_seq OWNED BY roles.id;
select setval('roles_id_seq', (SELECT MAX(id)+1 FROM roles), false);


CREATE TABLE IF NOT EXISTS skills
(
  id   BIGINT PRIMARY KEY NOT NULL,
  name VARCHAR(255)
);

ALTER TABLE skills ALTER COLUMN id SET DEFAULT nextval('skills_id_seq');
ALTER SEQUENCE skills_id_seq OWNED BY skills.id;
select setval('skills_id_seq', (SELECT MAX(id)+1 FROM skills), false);


CREATE TABLE IF NOT EXISTS states_provinces
(
  id         BIGINT PRIMARY KEY NOT NULL,
  country_id BIGINT             NOT NULL,
  name       VARCHAR(50),
  iso_code   VARCHAR(10)
);

ALTER TABLE states_provinces ALTER COLUMN id SET DEFAULT nextval('states_provinces_id_seq');
ALTER SEQUENCE states_provinces_id_seq OWNED BY states_provinces.id;
select setval('states_provinces_id_seq', (SELECT MAX(id)+1 FROM states_provinces), false);


CREATE TABLE IF NOT EXISTS studies
(
  id          BIGINT PRIMARY KEY NOT NULL,
  title       VARCHAR(1024)      NOT NULL,
  start_date  TIMESTAMP WITH TIME ZONE,
  end_date    TIMESTAMP WITH TIME ZONE,
  description VARCHAR(10000),
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE,
  status_id   BIGINT,
  type_id     BIGINT             NOT NULL
);

ALTER TABLE studies ALTER COLUMN id SET DEFAULT nextval('studies_id_seq');
ALTER SEQUENCE studies_id_seq OWNED BY studies.id;
select setval('studies_id_seq', (SELECT MAX(id)+1 FROM studies), false);


CREATE UNIQUE INDEX IF NOT EXISTS title_uk
  ON studies (title);

CREATE TABLE IF NOT EXISTS studies_data_sources
(
  study_id       BIGINT                                  NOT NULL,
  data_source_id BIGINT                                  NOT NULL,
  id             BIGINT PRIMARY KEY                      NOT NULL,
  status         VARCHAR(255)                            NOT NULL,
  created        TIMESTAMP DEFAULT now()                 NOT NULL,
  token          VARCHAR DEFAULT '' :: CHARACTER VARYING NOT NULL,
  created_by     BIGINT                                  NOT NULL,
  deleted_at     TIMESTAMP WITH TIME ZONE
);

ALTER TABLE studies_data_sources ALTER COLUMN id SET DEFAULT nextval('studies_data_sources_id_seq');
ALTER SEQUENCE studies_data_sources_id_seq OWNED BY studies_data_sources.id;
select setval('studies_data_sources_id_seq', (SELECT MAX(id)+1 FROM studies_data_sources), false);


CREATE TABLE IF NOT EXISTS studies_data_sources_comment
(
  id                      BIGINT PRIMARY KEY NOT NULL,
  comment                 VARCHAR            NOT NULL,
  studies_data_sources_id BIGINT,
  user_id                 BIGINT
);

ALTER TABLE studies_data_sources_comment ALTER COLUMN id SET DEFAULT nextval('studies_data_sources_comment_id_seq');
ALTER SEQUENCE studies_data_sources_comment_id_seq OWNED BY studies_data_sources_comment.id;
select setval('studies_data_sources_comment_id_seq', (SELECT MAX(id)+1 FROM studies_data_sources_comment), false);


CREATE UNIQUE INDEX IF NOT EXISTS studies_data_sources_comment_uk
  ON studies_data_sources_comment (studies_data_sources_id, user_id);

CREATE TABLE IF NOT EXISTS studies_files
(
  id           BIGINT PRIMARY KEY NOT NULL,
  study_id     BIGINT             NOT NULL,
  uuid         VARCHAR            NOT NULL,
  real_name    VARCHAR            NOT NULL,
  created      TIMESTAMP WITH TIME ZONE,
  updated      TIMESTAMP WITH TIME ZONE,
  mime_type    VARCHAR,
  label        VARCHAR,
  link         VARCHAR(2000),
  author_id    BIGINT,
  content_type VARCHAR
);

ALTER TABLE studies_files ALTER COLUMN id SET DEFAULT nextval('studies_files_id_seq');
ALTER SEQUENCE studies_files_id_seq OWNED BY studies_files.id;
select setval('studies_files_id_seq', (SELECT MAX(id)+1 FROM studies_files), false);


CREATE UNIQUE INDEX IF NOT EXISTS study_file_name_uk
  ON studies_files (uuid);

CREATE TABLE IF NOT EXISTS studies_users
(
  study_id   BIGINT                                  NOT NULL,
  user_id    BIGINT                                  NOT NULL,
  id         BIGINT PRIMARY KEY                      NOT NULL,
  role       VARCHAR(100),
  status     VARCHAR(255)                            NOT NULL,
  created    TIMESTAMP DEFAULT now()                 NOT NULL,
  token      VARCHAR DEFAULT '' :: CHARACTER VARYING NOT NULL,
  created_by BIGINT                                  NOT NULL,
  deleted_at TIMESTAMP WITH TIME ZONE,
  comment    VARCHAR
);

ALTER TABLE studies_users ALTER COLUMN id SET DEFAULT nextval('studies_users_id_seq');
ALTER SEQUENCE studies_users_id_seq OWNED BY studies_users.id;
select setval('studies_users_id_seq', (SELECT MAX(id)+1 FROM studies_users), false);


CREATE UNIQUE INDEX IF NOT EXISTS studies_users_uk
  ON studies_users (study_id, user_id);

CREATE TABLE IF NOT EXISTS study_statuses
(
  id   BIGINT PRIMARY KEY,
  name VARCHAR(255)
);

ALTER TABLE study_statuses ALTER COLUMN id SET DEFAULT nextval('study_statuses_id_seq');
ALTER SEQUENCE study_statuses_id_seq OWNED BY study_statuses.id;
select setval('study_statuses_id_seq', (SELECT MAX(id)+1 FROM study_statuses), false);


CREATE UNIQUE INDEX IF NOT EXISTS study_statuses_name_uk
  ON study_statuses (name);

CREATE TABLE IF NOT EXISTS study_types
(
  id   BIGINT PRIMARY KEY NOT NULL,
  name VARCHAR(255)
);

ALTER TABLE study_types ALTER COLUMN id SET DEFAULT nextval('study_types_id_seq');
ALTER SEQUENCE study_types_id_seq OWNED BY study_types.id;
select setval('study_types_id_seq', (SELECT MAX(id)+1 FROM study_types), false);


CREATE UNIQUE INDEX IF NOT EXISTS study_types_name_uk
  ON study_types (name);

CREATE TABLE IF NOT EXISTS submission_files
(
  id                  BIGINT PRIMARY KEY    NOT NULL,
  uuid                VARCHAR               NOT NULL,
  real_name           VARCHAR               NOT NULL,
  created             TIMESTAMP WITH TIME ZONE,
  updated             TIMESTAMP WITH TIME ZONE,
  mime_type           VARCHAR,
  label               VARCHAR,
  executable          BOOLEAN DEFAULT FALSE NOT NULL,
  submission_group_id BIGINT,
  author_id           BIGINT,
  version             INTEGER,
  checksum            CHAR(32),
  entry_point         VARCHAR,
  content_type        VARCHAR
);

ALTER TABLE submission_files ALTER COLUMN id SET DEFAULT nextval('submission_files_id_seq');
ALTER SEQUENCE submission_files_id_seq OWNED BY submission_files.id;
select setval('submission_files_id_seq', (SELECT MAX(id)+1 FROM submission_files), false);


CREATE UNIQUE INDEX IF NOT EXISTS submission_file_name_uk
  ON submission_files (uuid);

CREATE TABLE IF NOT EXISTS submission_groups
(
  id          BIGINT PRIMARY KEY NOT NULL,
  author_id   BIGINT             NOT NULL,
  analysis_id BIGINT             NOT NULL,
  created     TIMESTAMP WITH TIME ZONE,
  updated     TIMESTAMP WITH TIME ZONE,
  checksum    CHAR(32)
);

ALTER TABLE submission_groups ALTER COLUMN id SET DEFAULT nextval('submission_groups_id_seq');
ALTER SEQUENCE submission_groups_id_seq OWNED BY submission_groups.id;
select setval('submission_groups_id_seq', (SELECT MAX(id)+1 FROM submission_groups), false);


CREATE TABLE IF NOT EXISTS submission_insight_submission_files
(
  id                    BIGINT PRIMARY KEY NOT NULL,
  submission_insight_id BIGINT,
  submission_file_id    BIGINT,
  comment_topic_id      BIGINT
);

ALTER TABLE submission_insight_submission_files ALTER COLUMN id SET DEFAULT nextval('submission_insight_submission_files_id_seq');
ALTER SEQUENCE submission_insight_submission_files_id_seq OWNED BY submission_insight_submission_files.id;
select setval('submission_insight_submission_files_id_seq', (SELECT MAX(id)+1 FROM submission_insight_submission_files), false);


CREATE TABLE IF NOT EXISTS submission_insights
(
  id            BIGINT PRIMARY KEY                     NOT NULL,
  submission_id BIGINT                                 NOT NULL,
  created       TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
  name          VARCHAR                                NOT NULL,
  description   VARCHAR
);

ALTER TABLE submission_insights ALTER COLUMN id SET DEFAULT nextval('submission_insights_id_seq');
ALTER SEQUENCE submission_insights_id_seq OWNED BY submission_insights.id;
select setval('submission_insights_id_seq', (SELECT MAX(id)+1 FROM submission_insights), false);


CREATE TABLE IF NOT EXISTS submission_status_history
(
  id            BIGINT PRIMARY KEY NOT NULL,
  submission_id BIGINT             NOT NULL,
  date          TIMESTAMP          NOT NULL,
  status        VARCHAR            NOT NULL,
  author_id     BIGINT,
  is_last       BOOLEAN DEFAULT FALSE,
  comment       VARCHAR
);

ALTER TABLE submission_status_history ALTER COLUMN id SET DEFAULT nextval('submission_status_history_id_seq');
ALTER SEQUENCE submission_status_history_id_seq OWNED BY submission_status_history.id;
select setval('submission_status_history_id_seq', (SELECT MAX(id)+1 FROM submission_status_history), false);


CREATE INDEX IF NOT EXISTS ix_status_history_submission_id
  ON submission_status_history (submission_id);

CREATE TABLE IF NOT EXISTS submissions
(
  id                  BIGINT PRIMARY KEY NOT NULL,
  author_id           BIGINT             NOT NULL,
  created             TIMESTAMP WITH TIME ZONE,
  updated             TIMESTAMP WITH TIME ZONE,
  analysis_id         BIGINT             NOT NULL,
  data_source_id      BIGINT             NOT NULL,
  stdout              VARCHAR,
  stdout_date         TIMESTAMP WITH TIME ZONE,
  update_password     VARCHAR,
  submission_group_id BIGINT,
  token               VARCHAR(40)
);

ALTER TABLE submissions ALTER COLUMN id SET DEFAULT nextval('submissions_id_seq');
ALTER SEQUENCE submissions_id_seq OWNED BY submissions.id;
select setval('submissions_id_seq', (SELECT MAX(id)+1 FROM submissions), false);


CREATE TABLE IF NOT EXISTS system_settings
(
  id       BIGINT PRIMARY KEY NOT NULL,
  group_id BIGINT             NOT NULL,
  label    VARCHAR(255)       NOT NULL,
  name     VARCHAR(255)       NOT NULL,
  value    VARCHAR(1024),
  type     VARCHAR DEFAULT 'text' :: CHARACTER VARYING
);

ALTER TABLE system_settings ALTER COLUMN id SET DEFAULT nextval('system_settings_id_seq');
ALTER SEQUENCE system_settings_id_seq OWNED BY system_settings.id;
select setval('system_settings_id_seq', (SELECT MAX(id)+1 FROM system_settings), false);


CREATE TABLE IF NOT EXISTS system_settings_groups
(
  id    BIGINT PRIMARY KEY NOT NULL,
  label VARCHAR(255)       NOT NULL,
  name  VARCHAR(255)       NOT NULL
);

ALTER TABLE system_settings_groups ALTER COLUMN id SET DEFAULT nextval('system_settings_groups_id_seq');
ALTER SEQUENCE system_settings_groups_id_seq OWNED BY system_settings_groups.id;
select setval('system_settings_groups_id_seq', (SELECT MAX(id)+1 FROM system_settings_groups), false);


CREATE TABLE IF NOT EXISTS user_links
(
  id          BIGINT PRIMARY KEY NOT NULL,
  user_id     BIGINT             NOT NULL,
  title       VARCHAR(255)       NOT NULL,
  description VARCHAR(1024),
  url         VARCHAR(255)       NOT NULL
);

ALTER TABLE user_links ALTER COLUMN id SET DEFAULT nextval('user_links_id_seq');
ALTER SEQUENCE user_links_id_seq OWNED BY user_links.id;
select setval('user_links_id_seq', (SELECT MAX(id)+1 FROM user_links), false);


CREATE TABLE IF NOT EXISTS user_publications
(
  id          BIGINT PRIMARY KEY                                  NOT NULL,
  user_id     BIGINT                                              NOT NULL,
  title       VARCHAR(255)                                        NOT NULL,
  description VARCHAR(1024)                                       NOT NULL,
  date        TIMESTAMP DEFAULT now()                             NOT NULL,
  publisher   VARCHAR(255)                                        NOT NULL,
  url         VARCHAR(255) DEFAULT 'http://' :: CHARACTER VARYING NOT NULL
);

ALTER TABLE user_publications ALTER COLUMN id SET DEFAULT nextval('user_publications_id_seq');
ALTER SEQUENCE user_publications_id_seq OWNED BY user_publications.id;
select setval('user_publications_id_seq', (SELECT MAX(id)+1 FROM user_publications), false);


CREATE TABLE IF NOT EXISTS user_registrants
(
  id          BIGINT PRIMARY KEY NOT NULL,
  token       VARCHAR,
  sender_name VARCHAR,
  subject     VARCHAR,
  template    VARCHAR
);

ALTER TABLE user_registrants ALTER COLUMN id SET DEFAULT nextval('user_registrants_id_seq');
ALTER SEQUENCE user_registrants_id_seq OWNED BY user_registrants.id;
select setval('user_registrants_id_seq', (SELECT MAX(id)+1 FROM user_registrants), false);

CREATE UNIQUE INDEX IF NOT EXISTS user_registrants_token_unique ON user_registrants (token);

CREATE TABLE IF NOT EXISTS users
(
  email                VARCHAR(255)       NOT NULL,
  password             VARCHAR(512)       NOT NULL,
  enabled              BOOLEAN            NOT NULL,
  id                   BIGINT PRIMARY KEY NOT NULL,
  firstname            VARCHAR(100),
  middlename           VARCHAR(100),
  lastname             VARCHAR(100),
  last_password_reset  TIMESTAMP WITH TIME ZONE,
  created              TIMESTAMP WITH TIME ZONE,
  updated              TIMESTAMP WITH TIME ZONE,
  professional_type_id BIGINT,
  registration_code    VARCHAR(36),
  affiliation          VARCHAR(255),
  personal_summary     VARCHAR(255),
  contact_info         VARCHAR(255),
  uuid                 VARCHAR(50),
  email_confirmed      BOOLEAN            NOT NULL,
  phone                VARCHAR(30),
  mobile               VARCHAR(30),
  address1             VARCHAR(255),
  address2             VARCHAR(255),
  city                 VARCHAR(50),
  state_province_id    BIGINT,
  zip_code             VARCHAR(20),
  country_id           BIGINT,
  origin               VARCHAR,
  username             VARCHAR
);

ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq');
ALTER SEQUENCE users_id_seq OWNED BY users.id;
select setval('users_id_seq', (SELECT MAX(id)+1 FROM users), false);



ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_unique;
ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_origin_username_unique;
ALTER TABLE users ADD CONSTRAINT users_origin_username_unique UNIQUE (origin, username);

CREATE TABLE IF NOT EXISTS users_roles
(
  user_id BIGINT PRIMARY KEY NOT NULL,
  role_id BIGINT
);

CREATE TABLE IF NOT EXISTS users_skills
(
  user_id  BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  CONSTRAINT users_skills_pk PRIMARY KEY (user_id, skill_id)
);

CREATE OR REPLACE FUNCTION delete_studies_data_sources_comment()
  RETURNS TRIGGER AS $delete_studies_data_sources_comment$
DECLARE
BEGIN
  DELETE FROM studies_data_sources_comment
  WHERE studies_data_sources_id = OLD.id;
  RETURN NEW;
END;
$delete_studies_data_sources_comment$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_delete_studies_data_sources_comment ON studies_data_sources;
CREATE TRIGGER trigger_delete_studies_data_sources_comment
BEFORE UPDATE ON studies_data_sources
FOR EACH ROW
WHEN (NEW.status != 'DECLINED')
EXECUTE PROCEDURE delete_studies_data_sources_comment();

CREATE OR REPLACE FUNCTION set_last_submission_status()
  RETURNS TRIGGER AS $set_last_submission_status$
BEGIN
  --update all to 'false'
  UPDATE submission_status_history
  SET is_last = FALSE
  WHERE submission_id = NEW.submission_id;
  --update last to 'true'
  UPDATE submission_status_history
  SET is_last = TRUE
  WHERE id =
        (SELECT id
         FROM submission_status_history
         WHERE submission_id = NEW.submission_id
         ORDER BY date DESC
         LIMIT 1);
  RETURN NEW;
END;
$set_last_submission_status$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_last_submission_status ON submission_status_history;
CREATE TRIGGER set_last_submission_status
AFTER INSERT ON submission_status_history
FOR EACH ROW EXECUTE PROCEDURE set_last_submission_status();

CREATE OR REPLACE FUNCTION study_on_last_lead_cascade()
  RETURNS TRIGGER AS $study_on_last_lead_cascade$
DECLARE
  lead_cnt$ SMALLINT;
BEGIN
  SELECT COUNT(1)
  INTO lead_cnt$
  FROM studies_users
  WHERE study_id = OLD.study_id;

  IF (lead_cnt$ < 1)
  THEN
    DELETE FROM studies
    WHERE id = OLD.study_id;
  END IF;

  RETURN OLD;
END;
$study_on_last_lead_cascade$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_study_on_last_lead_cascade ON studies_users;
CREATE TRIGGER trg_study_on_last_lead_cascade
AFTER DELETE ON studies_users
FOR EACH ROW EXECUTE PROCEDURE study_on_last_lead_cascade();

CREATE OR REPLACE VIEW achilles_regular_files AS
  SELECT
    id AS achilles_file_id,
    characterization_id
  FROM achilles_files
  WHERE file_path SIMILAR TO '\w+.json';

CREATE OR REPLACE VIEW comment_topics_w_count AS
  SELECT
    comment_topics.*,
    (SELECT COUNT(1)
     FROM comments
     WHERE comments.topic_id = comment_topics.id) count
  FROM comment_topics;

DROP VIEW IF EXISTS users_studies_link CASCADE;
CREATE OR REPLACE VIEW users_studies_extended AS
  SELECT DISTINCT ON (data.study_id, data.user_id, data.role, data.status)
    row_number()
    OVER () AS id,
    *
  FROM (
         SELECT
           studies_users.study_id AS study_id,
           user_id,
           role :: VARCHAR,
           status :: VARCHAR,
           comment,
           NULL                   AS owned_data_source_id
         FROM studies_users

         UNION ALL

         SELECT
           studies_data_sources.study_id        AS study_id,
           datanodes_users.user_id              AS user_id,
           'DATA_SET_OWNER' :: VARCHAR          AS role,
           status :: VARCHAR                    AS status,
           studies_data_sources_comment.comment AS comment,
           data_sources.id                      AS owned_data_source_id
         FROM studies_data_sources
           JOIN data_sources ON data_sources.id = studies_data_sources.data_source_id
           JOIN datanodes ON datanodes.id = data_sources.data_node_id
           JOIN datanodes_users ON datanodes.id = datanodes_users.datanode_id
           JOIN datanodes_users_roles ON datanodes_users.id = datanodes_users_roles.datanode_user_id
           LEFT JOIN studies_data_sources_comment
             ON studies_data_sources_comment.studies_data_sources_id = studies_data_sources.id
                AND datanodes_users.user_id = studies_data_sources_comment.user_id
         WHERE datanodes_users_roles.datanode_role = 'ADMIN'
       ) AS data;

CREATE OR REPLACE VIEW studies_leads AS
  SELECT
    link.study_id,
    link.user_id,
    ROW_NUMBER()
    OVER (
      PARTITION BY link.study_id
      ORDER BY link.id ) row_number
  FROM users_studies_extended link
  WHERE link.role = 'LEAD_INVESTIGATOR' AND link.status = 'APPROVED';

DROP VIEW IF EXISTS users_studies_link_list CASCADE;
CREATE OR REPLACE VIEW users_studies_grouped AS
  SELECT
    row_number()
    OVER ()                             AS id,
    data.study_id,
    data.user_id,
    STRING_AGG(DISTINCT data.role, ',') AS roles,
    count(fs.*) > 0                     AS favourite,
    leads.user_id                       AS first_lead_id
  FROM users_studies_extended data
    JOIN studies_leads leads ON data.study_id = leads.study_id AND leads.row_number = 1
    LEFT JOIN favourite_studies fs ON data.study_id = fs.study_id AND data.user_id = fs.user_id
  WHERE data.status NOT IN ('DECLINED', 'DELETED')
  GROUP BY data.study_id, data.user_id, leads.user_id;

CREATE OR REPLACE VIEW studies_view AS
  WITH study_x_user AS (SELECT
                          s.id AS study_id,
                          u.id AS user_id
                        FROM studies s, users u
                        WHERE u.enabled IS TRUE AND u.email_confirmed IS TRUE),
      favourites AS (SELECT
                       study_id,
                       user_id,
                       TRUE AS favourite
                     FROM favourite_studies)
  SELECT
    row_number()
    OVER ()       AS id,
    sxu.*,
    fv.favourite,
    usll.roles,
    leads.user_id AS first_lead_id
  FROM study_x_user sxu
    LEFT JOIN users_studies_grouped usll ON sxu.study_id = usll.study_id AND sxu.user_id = usll.user_id
    JOIN studies_leads leads ON leads.study_id = sxu.study_id AND leads.row_number = 1
    LEFT JOIN favourites fv ON sxu.user_id = fv.user_id AND sxu.study_id = fv.study_id;

ALTER TABLE achilles_files ADD CONSTRAINT achilles_files__characterization_id_fkey FOREIGN KEY (characterization_id) REFERENCES characterizations (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE achilles_permissions ADD CONSTRAINT achilles_permissions__datasource_id_fkey FOREIGN KEY (datasource_id) REFERENCES data_sources (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE achilles_permissions ADD CONSTRAINT achilles_permissions__achilles_report_id_fkey FOREIGN KEY (achilles_report_id) REFERENCES achilles_reports (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE achilles_report_matchers ADD CONSTRAINT achilles_report_matchers__achilles_report_id_fkey FOREIGN KEY (achilles_report_id) REFERENCES achilles_reports (id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE analyses ADD CONSTRAINT analyses__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE analyses ADD CONSTRAINT analyses__study_id_fkey FOREIGN KEY (study_id) REFERENCES studies (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE analyses_files ADD CONSTRAINT analysis_files__analysis_id_fkey FOREIGN KEY (analysis_id) REFERENCES analyses (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE analyses_files ADD CONSTRAINT analyses_files__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE analyses_files ADD CONSTRAINT analyses_files__updated_by_fkey FOREIGN KEY (updated_by) REFERENCES users (id);
ALTER TABLE analyses_files ADD CONSTRAINT analyses_files__data_reference_id_fkey FOREIGN KEY (data_reference_id) REFERENCES data_references (id);

ALTER TABLE analysis_unlock_requests ADD CONSTRAINT analysis_unlock_requests__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE analysis_unlock_requests ADD CONSTRAINT analysis_unlock_requests__analysis_id_fkey FOREIGN KEY (analysis_id) REFERENCES analyses (id) ON DELETE CASCADE;
ALTER TABLE characterizations ADD CONSTRAINT characterizations__datasource_id_fkey FOREIGN KEY (datasource_id) REFERENCES data_sources (id) ON DELETE CASCADE;
ALTER TABLE comments ADD CONSTRAINT comments__parent_id_fkey FOREIGN KEY (parent_id) REFERENCES comments (id) ON DELETE CASCADE;
ALTER TABLE comments ADD CONSTRAINT comments__topic_id_fkey FOREIGN KEY (topic_id) REFERENCES comment_topics (id) ON DELETE CASCADE;
ALTER TABLE comments ADD CONSTRAINT comments__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id);
ALTER TABLE data_references ADD CONSTRAINT data_references__data_node_id_fkey FOREIGN KEY (data_node_id) REFERENCES datanodes (id);
ALTER TABLE data_source_health_check_journal ADD CONSTRAINT data_source_health_check_journal__data_source_id_fkey FOREIGN KEY (data_source_id) REFERENCES data_sources (id) ON DELETE CASCADE;
ALTER TABLE data_sources ADD CONSTRAINT data_sources__data_node_id_fkey FOREIGN KEY (data_node_id) REFERENCES datanodes (id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE datanodes ADD CONSTRAINT datanodes__status_id_fkey FOREIGN KEY (status_id) REFERENCES datanodes_statuses (id) ON UPDATE CASCADE;
ALTER TABLE datanodes_health_check_journal ADD CONSTRAINT datanodes_health_check_journal__data_node_id_fkey FOREIGN KEY (data_node_id) REFERENCES datanodes (id) ON DELETE CASCADE;
ALTER TABLE datanodes_journal ADD CONSTRAINT datanodes_journal__old_status_id_fkey FOREIGN KEY (old_status_id) REFERENCES datanodes_statuses (id) ON UPDATE CASCADE;
ALTER TABLE datanodes_journal ADD CONSTRAINT datanodes_journal__new_status_id_fkey FOREIGN KEY (new_status_id) REFERENCES datanodes_statuses (id) ON UPDATE CASCADE;
ALTER TABLE datanodes_journal ADD CONSTRAINT datanodes_journal__datanode_id_fkey FOREIGN KEY (datanode_id) REFERENCES datanodes (id) ON DELETE CASCADE;
ALTER TABLE datanodes_users ADD CONSTRAINT datanodes_users__datanode_id_fkey FOREIGN KEY (datanode_id) REFERENCES datanodes (id) ON DELETE CASCADE;
ALTER TABLE datanodes_users ADD CONSTRAINT datanodes_users__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE datanodes_users_roles ADD CONSTRAINT datanodes_users_roles__datanode_user_id_fkey FOREIGN KEY (datanode_user_id) REFERENCES datanodes_users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE favourite_studies ADD CONSTRAINT favourite_studies__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE favourite_studies ADD CONSTRAINT favourite_studies__study_id_fkey FOREIGN KEY (study_id) REFERENCES studies (id) ON DELETE CASCADE;
ALTER TABLE paper_favourites ADD CONSTRAINT paper_favourites__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE paper_favourites ADD CONSTRAINT paper_favourites__paper_id_fkey FOREIGN KEY (paper_id) REFERENCES papers (id);
ALTER TABLE paper_papers ADD CONSTRAINT paper_papers__paper_id_fkey FOREIGN KEY (paper_id) REFERENCES papers (id) ON DELETE CASCADE;
ALTER TABLE paper_papers ADD CONSTRAINT paper_papers__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id);
ALTER TABLE paper_protocols ADD CONSTRAINT paper_protocols__paper_id_fkey FOREIGN KEY (paper_id) REFERENCES papers (id) ON DELETE CASCADE;
ALTER TABLE paper_protocols ADD CONSTRAINT paper_protocols__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id);
ALTER TABLE papers ADD CONSTRAINT papers__study_id_fkey FOREIGN KEY (study_id) REFERENCES studies (id);
ALTER TABLE result_files ADD CONSTRAINT result_files__submission_id_fkey FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE result_files ADD CONSTRAINT result_files__comment_topic_id_fkey FOREIGN KEY (comment_topic_id) REFERENCES comment_topics (id);
ALTER TABLE states_provinces ADD CONSTRAINT states_provinces__country_id_fkey FOREIGN KEY (country_id) REFERENCES countries (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE studies ADD CONSTRAINT studies__status_id_fkey FOREIGN KEY (status_id) REFERENCES study_statuses (id) ON UPDATE CASCADE;
ALTER TABLE studies ADD CONSTRAINT studies__type_id_fkey FOREIGN KEY (type_id) REFERENCES study_types (id) ON UPDATE CASCADE;
ALTER TABLE studies_data_sources ADD CONSTRAINT studies_data_sources__study_id_fkey FOREIGN KEY (study_id) REFERENCES studies (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE studies_data_sources ADD CONSTRAINT studies_data_sources__data_source_id_fkey FOREIGN KEY (data_source_id) REFERENCES data_sources (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE studies_data_sources ADD CONSTRAINT studies_data_sources__created_by_fkey FOREIGN KEY (created_by) REFERENCES users (id) ON UPDATE CASCADE;
ALTER TABLE studies_data_sources_comment ADD CONSTRAINT studies_data_sources_comment__studies_data_sources_id_fkey FOREIGN KEY (studies_data_sources_id) REFERENCES studies_data_sources (id) ON DELETE CASCADE;
ALTER TABLE studies_data_sources_comment ADD CONSTRAINT studies_data_sources_comment__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE studies_files ADD CONSTRAINT studies_files__study_id_fkey FOREIGN KEY (study_id) REFERENCES studies (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE studies_files ADD CONSTRAINT studies_files__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE studies_users ADD CONSTRAINT studies_users__study_id_fkey FOREIGN KEY (study_id) REFERENCES studies (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE studies_users ADD CONSTRAINT studies_users__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE studies_users ADD CONSTRAINT studies_users__created_by_fkey FOREIGN KEY (created_by) REFERENCES users (id) ON UPDATE CASCADE;
ALTER TABLE submission_files ADD CONSTRAINT submission_files__submission_group_id_fkey FOREIGN KEY (submission_group_id) REFERENCES submission_groups (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE submission_files ADD CONSTRAINT submission_files__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id) ON UPDATE CASCADE;
ALTER TABLE submission_groups ADD CONSTRAINT submission_groups__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id) ON UPDATE CASCADE;
ALTER TABLE submission_groups ADD CONSTRAINT submission_groups__analysis_id_fkey FOREIGN KEY (analysis_id) REFERENCES analyses (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE submission_insight_submission_files ADD CONSTRAINT submission_insight_submission_files__submission_insight_id_fkey FOREIGN KEY (submission_insight_id) REFERENCES submission_insights (id) ON DELETE CASCADE;
ALTER TABLE submission_insight_submission_files ADD CONSTRAINT submission_insight_submission_files__submission_file_id_fkey FOREIGN KEY (submission_file_id) REFERENCES submission_files (id) ON DELETE CASCADE;
ALTER TABLE submission_insight_submission_files ADD CONSTRAINT submission_insight_submission_files__comment_topic_id_fkey FOREIGN KEY (comment_topic_id) REFERENCES comment_topics (id);
ALTER TABLE submission_insights ADD CONSTRAINT submission_insights__submission_id_fkey FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE;
ALTER TABLE submission_status_history ADD CONSTRAINT submission_status_history__submission_id_fkey FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE submission_status_history ADD CONSTRAINT submission_status_history__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id) ON UPDATE CASCADE;
ALTER TABLE submissions ADD CONSTRAINT submissions__author_id_fkey FOREIGN KEY (author_id) REFERENCES users (id) ON UPDATE CASCADE;
ALTER TABLE submissions ADD CONSTRAINT submissions__analysis_id_fkey FOREIGN KEY (analysis_id) REFERENCES analyses (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE submissions ADD CONSTRAINT submissions__data_source_id_fkey FOREIGN KEY (data_source_id) REFERENCES data_sources (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE submissions ADD CONSTRAINT submissions__submission_group_id_fkey FOREIGN KEY (submission_group_id) REFERENCES submission_groups (id);
ALTER TABLE system_settings ADD CONSTRAINT system_settings__group_id_fkey FOREIGN KEY (group_id) REFERENCES system_settings_groups (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE user_links ADD CONSTRAINT user_links__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE user_publications ADD CONSTRAINT user_publications__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE users ADD CONSTRAINT users__professional_type_id_fkey FOREIGN KEY (professional_type_id) REFERENCES professional_types (id) ON UPDATE CASCADE;
ALTER TABLE users ADD CONSTRAINT users__state_province_id_fkey FOREIGN KEY (state_province_id) REFERENCES states_provinces (id) ON UPDATE CASCADE;
ALTER TABLE users ADD CONSTRAINT users__country_id_fkey FOREIGN KEY (country_id) REFERENCES countries (id) ON UPDATE CASCADE;
ALTER TABLE users_roles ADD CONSTRAINT users_roles__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE users_roles ADD CONSTRAINT users_roles__role_id_fkey FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE users_skills ADD CONSTRAINT users_skills__user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE users_skills ADD CONSTRAINT users_skills__skill_id_fkey FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE ON UPDATE CASCADE;

INSERT INTO study_statuses (name) VALUES ('Initiate') ON CONFLICT (name) DO NOTHING;
INSERT INTO study_statuses (name) VALUES ('Active') ON CONFLICT (name) DO NOTHING;
INSERT INTO study_statuses (name) VALUES ('Completed') ON CONFLICT (name) DO NOTHING;
INSERT INTO study_statuses (name) VALUES ('Archived') ON CONFLICT (name) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS system_settings_groups_label_unique
  ON system_settings_groups (name);

INSERT INTO system_settings_groups (label, name) VALUES ('Integration', 'integration') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings_groups (label, name) VALUES ('Mail server', 'mail.server') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings_groups (label, name) VALUES ('Auth', 'auth') ON CONFLICT (name) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS system_settings_name_unique
  ON system_settings (name);

INSERT INTO system_settings (group_id, label, name, value, type) VALUES (1, 'Central (this) url', 'portal.url', null, 'text') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (1, 'Solr server url', 'arachne.solrServerUrl', null, 'text') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (1, 'Zookeper host', 'zookeper.host', null, 'text') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (1, 'Zookeper port', 'zookeper.port', null, 'text') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (2, 'Host', 'spring.mail.host', null, 'text') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (2, 'Port', 'spring.mail.port', null, 'text') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (2, 'Username', 'spring.mail.username', null, 'text') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (3, 'Auth method', 'portal.authMethod', null, 'text') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (2, 'Password', 'spring.mail.password', null, 'password') ON CONFLICT (name) DO NOTHING;
INSERT INTO system_settings (group_id, label, name, value, type) VALUES (1, 'SSL enabled', 'server.ssl.enabled', null, 'checkbox') ON CONFLICT (name) DO NOTHING;


INSERT INTO study_types (name) VALUES ('Safety and Efficacy') ON CONFLICT (name) DO NOTHING;
INSERT INTO study_types (name) VALUES ('Clinical Trial Design') ON CONFLICT (name) DO NOTHING;
INSERT INTO study_types (name) VALUES ('Clinical Trial Patient Enrollment') ON CONFLICT (name) DO NOTHING;
INSERT INTO study_types (name) VALUES ('Health Economics and Outcomes') ON CONFLICT (name) DO NOTHING;
INSERT INTO study_types (name) VALUES ('Sales and Marketing') ON CONFLICT (name) DO NOTHING;
INSERT INTO study_types (name) VALUES ('Other') ON CONFLICT (name) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS professional_types_name_unique
  ON professional_types (name);

INSERT INTO professional_types (name) VALUES ('IT Professional') ON CONFLICT (name) DO NOTHING;
INSERT INTO professional_types (name) VALUES ('Medical') ON CONFLICT (name) DO NOTHING;
INSERT INTO professional_types (name) VALUES ('Epidemiology') ON CONFLICT (name) DO NOTHING;
INSERT INTO professional_types (name) VALUES ('Drug Safety') ON CONFLICT (name) DO NOTHING;
INSERT INTO professional_types (name) VALUES ('Sales & Marketing') ON CONFLICT (name) DO NOTHING;
INSERT INTO professional_types (name) VALUES ('Finance') ON CONFLICT (name) DO NOTHING;
INSERT INTO professional_types (name) VALUES ('Academic Research') ON CONFLICT (name) DO NOTHING;

INSERT INTO studies (title, start_date, end_date, description, created, updated, status_id, type_id) VALUES ('Study #1', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000', 'descr', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000',
        (SELECT id FROM study_statuses where name = 'Active'),(SELECT id FROM study_types WHERE name = 'Other')) ON CONFLICT (title) DO NOTHING;
INSERT INTO studies (title, start_date, end_date, description, created, updated, status_id, type_id) VALUES ('Study #2', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000', 'descr', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000',
        (SELECT id FROM study_statuses where name = 'Active'),(SELECT id FROM study_types WHERE name = 'Other')) ON CONFLICT (title) DO NOTHING;
INSERT INTO studies (title, start_date, end_date, description, created, updated, status_id, type_id) VALUES ('Study #3', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000', 'descr', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000',
        (SELECT id FROM study_statuses where name = 'Active'),(SELECT id FROM study_types WHERE name = 'Other')) ON CONFLICT (title) DO NOTHING;
INSERT INTO studies (title, start_date, end_date, description, created, updated, status_id, type_id) VALUES ('Study #4', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000', 'descr', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000',
        (SELECT id FROM study_statuses where name = 'Active'),(SELECT id FROM study_types WHERE name = 'Other')) ON CONFLICT (title) DO NOTHING;
INSERT INTO studies (title, start_date, end_date, description, created, updated, status_id, type_id) VALUES ('Study #5', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000', 'descr', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000',
        (SELECT id FROM study_statuses where name = 'Active'),(SELECT id FROM study_types WHERE name = 'Other')) ON CONFLICT (title) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS countries_name_unique
  ON countries (name);

INSERT INTO countries (name, alfa2, alfa3) VALUES ('Afghanistan', 'AF', 'AFG') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Aland Islands', 'AX', 'ALA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Albania', 'AL', 'ALB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Algeria', 'DZ', 'DZA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('American Samoa', 'AS', 'ASM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Andorra', 'AD', 'AND') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Angola', 'AO', 'AGO') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Anguilla', 'AI', 'AIA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Antarctica', 'AQ', 'ATA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Antigua and Barbuda', 'AG', 'ATG') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Argentina', 'AR', 'ARG') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Armenia', 'AM', 'ARM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Aruba', 'AW', 'ABW') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Australia', 'AU', 'AUS') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Austria', 'AT', 'AUT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Azerbaijan', 'AZ', 'AZE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bahamas (the)', 'BS', 'BHS') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bahrain', 'BH', 'BHR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bangladesh', 'BD', 'BGD') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Barbados', 'BB', 'BRB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Belarus', 'BY', 'BLR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Belgium', 'BE', 'BEL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Belize', 'BZ', 'BLZ') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Benin', 'BJ', 'BEN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bermuda', 'BM', 'BMU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bhutan', 'BT', 'BTN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bolivia (Plurinational State of)', 'BO', 'BOL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bonaire, Sint Eustatius and Saba', 'BQ', 'BES') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bosnia and Herzegovina', 'BA', 'BIH') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Botswana', 'BW', 'BWA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bouvet Island', 'BV', 'BVT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Brazil', 'BR', 'BRA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('British Indian Ocean Territory (the)', 'IO', 'IOT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Brunei Darussalam', 'BN', 'BRN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Bulgaria', 'BG', 'BGR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Burkina Faso', 'BF', 'BFA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Burundi', 'BI', 'BDI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cabo Verde', 'CV', 'CPV') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cambodia', 'KH', 'KHM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cameroon', 'CM', 'CMR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Canada', 'CA', 'CAN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cayman Islands (the)', 'KY', 'CYM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Central African Republic (the)', 'CF', 'CAF') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Chad', 'TD', 'TCD') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Chile', 'CL', 'CHL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('China', 'CN', 'CHN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Christmas Island', 'CX', 'CXR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cocos (Keeling) Islands (the)', 'CC', 'CCK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Colombia', 'CO', 'COL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Comoros (the)', 'KM', 'COM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Congo (the Democratic Republic of the)', 'CD', 'COD') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Congo (the)', 'CG', 'COG') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cook Islands (the)', 'CK', 'COK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Costa Rica', 'CR', 'CRI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cote d''Ivoire', 'CI', 'CIV') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Croatia', 'HR', 'HRV') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cuba', 'CU', 'CUB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Curacao', 'CW', 'CUW') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Cyprus', 'CY', 'CYP') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Czechia', 'CZ', 'CZE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Denmark', 'DK', 'DNK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Djibouti', 'DJ', 'DJI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Dominica', 'DM', 'DMA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Dominican Republic (the)', 'DO', 'DOM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Ecuador', 'EC', 'ECU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Egypt', 'EG', 'EGY') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('El Salvador', 'SV', 'SLV') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Equatorial Guinea', 'GQ', 'GNQ') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Eritrea', 'ER', 'ERI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Estonia', 'EE', 'EST') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Ethiopia', 'ET', 'ETH') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Falkland Islands (the) [Malvinas]', 'FK', 'FLK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Faroe Islands (the)', 'FO', 'FRO') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Fiji', 'FJ', 'FJI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Finland', 'FI', 'FIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('France', 'FR', 'FRA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('French Guiana', 'GF', 'GUF') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('French Polynesia', 'PF', 'PYF') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('French Southern Territories (the)', 'TF', 'ATF') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Gabon', 'GA', 'GAB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Gambia (the)', 'GM', 'GMB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Georgia', 'GE', 'GEO') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Germany', 'DE', 'DEU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Ghana', 'GH', 'GHA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Gibraltar', 'GI', 'GIB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Greece', 'GR', 'GRC') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Greenland', 'GL', 'GRL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Grenada', 'GD', 'GRD') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Guadeloupe', 'GP', 'GLP') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Guam', 'GU', 'GUM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Guatemala', 'GT', 'GTM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Guernsey', 'GG', 'GGY') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Guinea', 'GN', 'GIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Guinea-Bissau', 'GW', 'GNB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Guyana', 'GY', 'GUY') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Haiti', 'HT', 'HTI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Heard Island and McDonald Islands', 'HM', 'HMD') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Holy See (the)', 'VA', 'VAT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Honduras', 'HN', 'HND') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Hong Kong', 'HK', 'HKG') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Hungary', 'HU', 'HUN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Iceland', 'IS', 'ISL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('India', 'IN', 'IND') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Indonesia', 'ID', 'IDN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Iran (Islamic Republic of)', 'IR', 'IRN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Iraq', 'IQ', 'IRQ') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Ireland', 'IE', 'IRL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Isle of Man', 'IM', 'IMN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Israel', 'IL', 'ISR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Italy', 'IT', 'ITA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Jamaica', 'JM', 'JAM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Japan', 'JP', 'JPN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Jersey', 'JE', 'JEY') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Jordan', 'JO', 'JOR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Kazakhstan', 'KZ', 'KAZ') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Kenya', 'KE', 'KEN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Kiribati', 'KI', 'KIR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Korea (the Democratic People''s Republic of)', 'KP', 'PRK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Korea (the Republic of)', 'KR', 'KOR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Kuwait', 'KW', 'KWT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Kyrgyzstan', 'KG', 'KGZ') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Lao People''s Democratic Republic (the)', 'LA', 'LAO') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Latvia', 'LV', 'LVA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Lebanon', 'LB', 'LBN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Lesotho', 'LS', 'LSO') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Liberia', 'LR', 'LBR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Libya', 'LY', 'LBY') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Liechtenstein', 'LI', 'LIE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Lithuania', 'LT', 'LTU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Luxembourg', 'LU', 'LUX') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Macao', 'MO', 'MAC') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Macedonia (the former Yugoslav Republic of)', 'MK', 'MKD') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Madagascar', 'MG', 'MDG') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Malawi', 'MW', 'MWI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Malaysia', 'MY', 'MYS') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Maldives', 'MV', 'MDV') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Mali', 'ML', 'MLI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Malta', 'MT', 'MLT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Marshall Islands (the)', 'MH', 'MHL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Martinique', 'MQ', 'MTQ') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Mauritania', 'MR', 'MRT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Mauritius', 'MU', 'MUS') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Mayotte', 'YT', 'MYT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Mexico', 'MX', 'MEX') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Micronesia (Federated States of)', 'FM', 'FSM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Moldova (the Republic of)', 'MD', 'MDA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Monaco', 'MC', 'MCO') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Mongolia', 'MN', 'MNG') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Montenegro', 'ME', 'MNE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Montserrat', 'MS', 'MSR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Morocco', 'MA', 'MAR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Mozambique', 'MZ', 'MOZ') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Myanmar', 'MM', 'MMR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Namibia', 'NA', 'NAM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Nauru', 'NR', 'NRU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Nepal', 'NP', 'NPL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Netherlands (the)', 'NL', 'NLD') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('New Caledonia', 'NC', 'NCL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('New Zealand', 'NZ', 'NZL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Nicaragua', 'NI', 'NIC') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Niger (the)', 'NE', 'NER') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Nigeria', 'NG', 'NGA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Niue', 'NU', 'NIU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Norfolk Island', 'NF', 'NFK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Northern Mariana Islands (the)', 'MP', 'MNP') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Norway', 'NO', 'NOR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Oman', 'OM', 'OMN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Pakistan', 'PK', 'PAK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Palau', 'PW', 'PLW') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Palestine, State of', 'PS', 'PSE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Panama', 'PA', 'PAN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Papua New Guinea', 'PG', 'PNG') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Paraguay', 'PY', 'PRY') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Peru', 'PE', 'PER') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Philippines (the)', 'PH', 'PHL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Pitcairn', 'PN', 'PCN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Poland', 'PL', 'POL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Portugal', 'PT', 'PRT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Puerto Rico', 'PR', 'PRI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Qatar', 'QA', 'QAT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Reunion', 'RE', 'REU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Romania', 'RO', 'ROU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Russian Federation (the)', 'RU', 'RUS') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Rwanda', 'RW', 'RWA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Saint Barthelemy', 'BL', 'BLM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Saint Helena, Ascension and Tristan da Cunha', 'SH', 'SHN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Saint Kitts and Nevis', 'KN', 'KNA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Saint Lucia', 'LC', 'LCA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Saint Martin (French part)', 'MF', 'MAF') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Saint Pierre and Miquelon', 'PM', 'SPM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Saint Vincent and the Grenadines', 'VC', 'VCT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Samoa', 'WS', 'WSM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('San Marino', 'SM', 'SMR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Sao Tome and Principe', 'ST', 'STP') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Saudi Arabia', 'SA', 'SAU') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Senegal', 'SN', 'SEN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Serbia', 'RS', 'SRB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Seychelles', 'SC', 'SYC') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Sierra Leone', 'SL', 'SLE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Singapore', 'SG', 'SGP') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Sint Maarten (Dutch part)', 'SX', 'SXM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Slovakia', 'SK', 'SVK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Slovenia', 'SI', 'SVN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Solomon Islands', 'SB', 'SLB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Somalia', 'SO', 'SOM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('South Africa', 'ZA', 'ZAF') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('South Georgia and the South Sandwich Islands', 'GS', 'SGS') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('South Sudan', 'SS', 'SSD') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Spain', 'ES', 'ESP') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Sri Lanka', 'LK', 'LKA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Sudan (the)', 'SD', 'SDN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Suriname', 'SR', 'SUR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Svalbard and Jan Mayen', 'SJ', 'SJM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Swaziland', 'SZ', 'SWZ') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Sweden', 'SE', 'SWE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Switzerland', 'CH', 'CHE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Syrian Arab Republic', 'SY', 'SYR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Taiwan (Province of China)', 'TW', 'TWN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Tajikistan', 'TJ', 'TJK') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Tanzania, United Republic of', 'TZ', 'TZA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Thailand', 'TH', 'THA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Timor-Leste', 'TL', 'TLS') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Togo', 'TG', 'TGO') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Tokelau', 'TK', 'TKL') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Tonga', 'TO', 'TON') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Trinidad and Tobago', 'TT', 'TTO') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Tunisia', 'TN', 'TUN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Turkey', 'TR', 'TUR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Turkmenistan', 'TM', 'TKM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Turks and Caicos Islands (the)', 'TC', 'TCA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Tuvalu', 'TV', 'TUV') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Uganda', 'UG', 'UGA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Ukraine', 'UA', 'UKR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('United Arab Emirates (the)', 'AE', 'ARE') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('United Kingdom of Great Britain and Northern Ireland (the)', 'GB', 'GBR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('United States Minor Outlying Islands (the)', 'UM', 'UMI') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('United States of America (the)', 'US', 'USA') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Uruguay', 'UY', 'URY') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Uzbekistan', 'UZ', 'UZB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Vanuatu', 'VU', 'VUT') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Venezuela (Bolivarian Republic of)', 'VE', 'VEN') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Viet Nam', 'VN', 'VNM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Virgin Islands (British)', 'VG', 'VGB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Virgin Islands (U.S.)', 'VI', 'VIR') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Wallis and Futuna', 'WF', 'WLF') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Western Sahara*', 'EH', 'ESH') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Yemen', 'YE', 'YEM') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Zambia', 'ZM', 'ZMB') ON CONFLICT (name) DO NOTHING;
INSERT INTO countries (name, alfa2, alfa3) VALUES ('Zimbabwe', 'ZW', 'ZWE') ON CONFLICT (name) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS states_provinces_country_id_name_unique
  ON states_provinces (country_id, name);

INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'Alabama', 'US-AL') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'Alaska', 'US-AK') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'Arizona', 'US-AZ') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'Arkansas', 'US-AR') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'California', 'US-CA') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'Colorado', 'US-CO') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'Connecticut', 'US-CT') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'Delaware', 'US-DE') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'Austria'), 'Florida', 'US-FL') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Georgia', 'US-GA') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Hawaii', 'US-HI') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Idaho', 'US-ID') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Illinois', 'US-IL') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Indiana', 'US-IN') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Iowa', 'US-IA') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Kansas', 'US-KS') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Kentucky', 'US-KY') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Louisiana', 'US-LA') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Maine', 'US-ME') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Maryland', 'US-MD') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Massachusetts', 'US-MA') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Michigan', 'US-MI') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Minnesota', 'US-MN') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Mississippi', 'US-MS') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Missouri', 'US-MO') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Montana', 'US-MT') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Nebraska', 'US-NE') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Nevada', 'US-NV') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'New Hampshire', 'US-NH') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'New Jersey', 'US-NJ') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'New Mexico', 'US-NM') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'New York', 'US-NY') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'North Carolina', 'US-NC') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), ' North Dakota', 'US-ND') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Ohio', 'US-OH') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Oklahoma', 'US-OK') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Oregon', 'US-OR') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Pennsylvania', 'US-PA') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Rhode Island', 'US-RI') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'South Carolina', 'US-SC') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'South Dakota', 'US-SD') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Tennessee', 'US-TN') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Texas', 'US-TX') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Utah', 'US-UT') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Vermont', 'US-VT') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Virginia', 'US-VA') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Washington', 'US-WA') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'West Virginia', 'US-WV') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Wisconsin', 'US-WI') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Wyoming', 'US-WY') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'District of Columbia', 'US-DC') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'American Samoa', 'US-AS') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Guam', 'US-GU') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Northern Mariana Islands', 'US-MP') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Puerto Rico', 'US-PR') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'United States Minor Outlying Islands', 'US-UM') ON CONFLICT (country_id, name) DO NOTHING;
INSERT INTO states_provinces (country_id, name, iso_code) VALUES ((SELECT id FROM countries WHERE name = 'United States of America (the)'), 'Virgin Islands, U.S.', 'US-VI') ON CONFLICT (country_id, name) DO NOTHING;

INSERT INTO datanodes_statuses (name) VALUES ('New') ON CONFLICT (name) DO NOTHING;
INSERT INTO datanodes_statuses (name) VALUES ('Approved') ON CONFLICT (name) DO NOTHING;
INSERT INTO datanodes_statuses (name) VALUES ('Rejected') ON CONFLICT (name) DO NOTHING;
INSERT INTO datanodes_statuses (name) VALUES ('Active') ON CONFLICT (name) DO NOTHING;
INSERT INTO datanodes_statuses (name) VALUES ('Inactive') ON CONFLICT (name) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS achilles_reports_unique_name
  ON achilles_reports (name);

INSERT INTO achilles_reports (label, name, sort_order) VALUES ('dashboard', 'Dashboard', 10) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('achillesheel', 'Achilles Heel', 20) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('person', 'Person', 30) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('observationperiods', 'Observation Periods', 40) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('datadensity', 'Data Density', 50) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('conditions', 'Conditions', 60) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('conditionera', 'Condition Eras', 70) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('observations', 'Observations', 80) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('drugeras', 'Drug Eras', 90) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('drugexposures', 'Drug Exposures', 100) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('procedures', 'Procedures', 110) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('visits', 'Visits', 120) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_reports (label, name, sort_order) VALUES ('death', 'Death', 130) ON CONFLICT (name) DO NOTHING;

ALTER TABLE achilles_report_matchers DROP CONSTRAINT IF EXISTS achilles_report_matchers_pattern_unique;
ALTER TABLE achilles_report_matchers ADD CONSTRAINT achilles_report_matchers_pattern_unique UNIQUE (pattern);

INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Dashboard'), 'dashboard.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Achilles Heel'), 'achillesheel.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Achilles Heel'), 'person.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Observation Periods'), 'observationperiod.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Data Density'), 'datadensity.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Conditions'), 'condition_treemap.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Conditions'), 'conditions/condition_*.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Condition Eras'), 'conditionera_treemap.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Condition Eras'), 'conditioneras/condition_*.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Observations'), 'observation_treemap.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Observations'), 'observations/observation_*.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Drug Eras'), 'drugera_treemap.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Drug Eras'), 'drugeras/drug_*.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Drug Exposures'), 'drug_treemap.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Drug Exposures'), 'drugs/drug_*.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Procedures'), 'procedure_treemap.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Procedures'),' procedures/procedure_*.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Visits'), 'visit_treemap.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Visits'), 'visits/visit_*.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Death'), 'death.json') ON CONFLICT (pattern) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS roles_name_unique
  ON roles (name);

INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', 'Built-in Administration Role') ON CONFLICT (name) DO NOTHING;

INSERT INTO users (email, password, enabled, firstname, middlename, lastname, last_password_reset, created, updated, professional_type_id, registration_code, affiliation, personal_summary, contact_info, uuid, email_confirmed, phone, mobile, address1, address2, city, state_province_id, zip_code, country_id, origin, username) VALUES ('mr_lead_investigator@example.com', '$2a$10$36vcvLu3wqZ7NwAcRJg3L.pfr6Kc.36X6BEnbpEKJ1RnsWXOmBdjC', true, 'admin', 'admin', 'admin', '2017-01-15 00:00:00.000000', '2017-01-15 00:00:00.000000', '2017-01-15 00:00:00.000000', (SELECT id FROM professional_types WHERE name = 'IT Professional'), null, null, null, null, null, true, null, null, null, null, null, null, null, null, 'NATIVE', 'mr_lead_investigator@example.com') ON CONFLICT ON CONSTRAINT users_email_unique DO NOTHING;
INSERT INTO users (email, password, enabled, firstname, middlename, lastname, last_password_reset, created, updated, professional_type_id, registration_code, affiliation, personal_summary, contact_info, uuid, email_confirmed, phone, mobile, address1, address2, city, state_province_id, zip_code, country_id, origin, username) VALUES ('mr_data_set_owner@example.com', '$2a$10$36vcvLu3wqZ7NwAcRJg3L.pfr6Kc.36X6BEnbpEKJ1RnsWXOmBdjC', true, 'admin', 'admin', 'admin', '2017-01-15 00:00:00.000000', '2017-01-15 00:00:00.000000', '2017-01-15 00:00:00.000000', (SELECT id FROM professional_types WHERE name = 'IT Professional'), null, null, null, null, null, true, null, null, null, null, null, null, null, null, 'NATIVE', 'mr_data_set_owner@example.com') ON CONFLICT ON CONSTRAINT users_email_unique DO NOTHING;
INSERT INTO users (email, password, enabled, firstname, middlename, lastname, last_password_reset, created, updated, professional_type_id, registration_code, affiliation, personal_summary, contact_info, uuid, email_confirmed, phone, mobile, address1, address2, city, state_province_id, zip_code, country_id, origin, username) VALUES ('mr_collaborator@example.com', '$2a$10$36vcvLu3wqZ7NwAcRJg3L.pfr6Kc.36X6BEnbpEKJ1RnsWXOmBdjC', true, 'admin', 'admin', 'admin', '2017-01-15 00:00:00.000000', '2017-01-15 00:00:00.000000', '2017-01-15 00:00:00.000000', (SELECT id FROM professional_types WHERE name = 'IT Professional'), null, null, null, null, null, true, null, null, null, null, null, null, null, null, 'NATIVE', 'mr_collaborator@example.com') ON CONFLICT ON CONSTRAINT users_email_unique DO NOTHING;
INSERT INTO users (email, password, enabled, firstname, middlename, lastname, last_password_reset, created, updated, professional_type_id, registration_code, affiliation, personal_summary, contact_info, uuid, email_confirmed, phone, mobile, address1, address2, city, state_province_id, zip_code, country_id, origin, username) VALUES ('admin@odysseusinc.com', '$2a$10$36vcvLu3wqZ7NwAcRJg3L.pfr6Kc.36X6BEnbpEKJ1RnsWXOmBdjC', true, 'admin', 'admin', 'admin', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000', '2017-09-27 00:00:00.000000', (SELECT id FROM professional_types WHERE name = 'IT Professional'), null, null, null, null, null, true, null, null, null, null, null, null, null, null, 'NATIVE', 'admin@odysseusinc.com') ON CONFLICT ON CONSTRAINT users_email_unique DO NOTHING;

INSERT INTO users_roles (user_id, role_id) VALUES ((SELECT id FROM users WHERE email = 'admin@odysseusinc.com'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')) ON CONFLICT (user_id) DO NOTHING;