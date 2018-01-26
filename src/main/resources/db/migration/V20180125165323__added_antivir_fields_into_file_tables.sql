ALTER TABLE studies_files
  ADD COLUMN antivirus_status VARCHAR NOT NULL DEFAULT 'WILL_NOT_SCAN',
  ADD COLUMN antivirus_description VARCHAR;
ALTER TABLE paper_papers
  ADD COLUMN antivirus_status VARCHAR NOT NULL DEFAULT 'WILL_NOT_SCAN',
  ADD COLUMN antivirus_description VARCHAR;
ALTER TABLE paper_protocols
  ADD COLUMN antivirus_status VARCHAR NOT NULL DEFAULT 'WILL_NOT_SCAN',
  ADD COLUMN antivirus_description VARCHAR;
ALTER TABLE analyses_files
  ADD COLUMN antivirus_status VARCHAR NOT NULL DEFAULT 'WILL_NOT_SCAN',
  ADD COLUMN antivirus_description VARCHAR;
ALTER TABLE submission_files
  ADD COLUMN antivirus_status VARCHAR NOT NULL DEFAULT 'WILL_NOT_SCAN',
  ADD COLUMN antivirus_description VARCHAR;