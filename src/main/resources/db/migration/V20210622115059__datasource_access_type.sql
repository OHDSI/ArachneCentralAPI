ALTER TABLE data_sources_data
    ADD COLUMN IF NOT EXISTS access_type VARCHAR NULL DEFAULT 'PUBLIC';

ALTER TABLE data_sources_data DROP CONSTRAINT data_sources_data_not_blank_fields_if_published;

ALTER TABLE data_sources_data ADD CONSTRAINT data_sources_data_not_blank_fields_if_published
    CHECK (published = FALSE OR (organization <> '' AND model_type <> '' AND access_type <> ''));
