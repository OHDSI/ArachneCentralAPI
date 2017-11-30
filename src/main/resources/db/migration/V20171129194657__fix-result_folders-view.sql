CREATE OR REPLACE VIEW v_result_files_folders AS
SELECT DISTINCT ON (submission_id, real_name)
  -1::BIGINT AS id,
  rf.submission_id,
  NULL::VARCHAR AS uuid,
  array_to_string((string_to_array(rf.real_name, '/'))[1:idx], '/') real_name,
  rf.created AS created,
  rf.updated AS updated,
  NULL::VARCHAR AS mime_type,
  NULL::VARCHAR AS label,
  NULL::BIGINT AS comment_topic_id,
  FALSE AS manual_upload,
  'folder'::VARCHAR content_type
FROM result_files rf,
      unnest(string_to_array(rf.real_name, '/')) with ordinality as x(path_part, idx)
WHERE rf.real_name != array_to_string((string_to_array(rf.real_name, '/'))[1:idx], '/');