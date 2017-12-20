DROP VIEW v_result_files_with_folders;
DROP VIEW v_result_files_folders;

ALTER TABLE result_files RENAME uuid TO legacy_uuid;
ALTER TABLE result_files ALTER legacy_uuid DROP NOT NULL;

ALTER TABLE result_files RENAME real_name TO legacy_real_name;
ALTER TABLE result_files ALTER legacy_real_name DROP NOT NULL;

ALTER TABLE result_files RENAME manual_upload TO legacy_manual_upload;
ALTER TABLE result_files ALTER legacy_manual_upload DROP NOT NULL;

ALTER TABLE result_files DROP COLUMN IF EXISTS created;
ALTER TABLE result_files DROP COLUMN IF EXISTS updated;
ALTER TABLE result_files DROP COLUMN IF EXISTS mime_type;
ALTER TABLE result_files DROP COLUMN IF EXISTS label;
ALTER TABLE result_files DROP COLUMN IF EXISTS content_type;

ALTER TABLE result_files ADD COLUMN IF NOT EXISTS path VARCHAR;
ALTER TABLE result_files ADD COLUMN IF NOT EXISTS uuid VARCHAR;