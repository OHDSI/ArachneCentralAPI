ALTER TABLE tenants_users DROP CONSTRAINT tenants_users_user_id_fkey;
ALTER TABLE tenants_users
  ADD CONSTRAINT tenants_users_user_id_fkey
FOREIGN KEY (user_id) REFERENCES users_data (id) ON DELETE CASCADE;

DROP FUNCTION check_if_users_are_deletable(VARCHAR, VARCHAR);
CREATE OR REPLACE FUNCTION check_if_users_are_deletable(ids VARCHAR, excluded_tables VARCHAR)
  RETURNS VARCHAR AS $$
declare
  ids_array INT[];
  selected_ids INT[];
  filtered_ids INT[];
  excluded_array VARCHAR[];
  table_name VARCHAR;
  column_name VARCHAR;
begin
  ids_array := string_to_array(ids, ',');
  excluded_array := string_to_array(excluded_tables, ',');
  filtered_ids := ids_array;
  FOR table_name, column_name IN
  SELECT DISTINCT tc.table_name, kcu.column_name
  FROM
    information_schema.table_constraints AS tc
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
  WHERE constraint_type = 'FOREIGN KEY' AND ccu.table_name = 'users_data' AND tc.table_name != ANY(excluded_array)
  LOOP
    EXECUTE 'SELECT ARRAY(SELECT DISTINCT ' || column_name ||'::INT FROM ' || table_name || ' WHERE ' || column_name || '::INT = ANY($1))'
    INTO selected_ids
    USING filtered_ids;
    SELECT ARRAY(SELECT unnest(filtered_ids)
                 EXCEPT
                 SELECT unnest(selected_ids))
    INTO filtered_ids;
  end loop;
  return array_to_string(filtered_ids, ',');
end;
$$ language plpgsql;