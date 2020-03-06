CREATE OR REPLACE VIEW users_studies_extended AS
SELECT DISTINCT ON (data.study_id, data.user_id, data.role, data.status, data.owned_data_source_id)
        row_number() OVER () AS id,
        study_id,
        user_id,
        role,
        status,
        comment,
        owned_data_source_id
FROM (
         SELECT studies_users.study_id AS study_id,
                user_id,
                role :: VARCHAR,
                status :: VARCHAR,
                comment,
                NULL                   AS owned_data_source_id
         FROM studies_users

         UNION ALL

         SELECT studies_data_sources.study_id        AS study_id,
                datanodes_users.user_id              AS user_id,
                'DATA_SET_OWNER' :: VARCHAR          AS role,
                status :: VARCHAR                    AS status,
                studies_data_sources_comment.comment AS comment,
                data_sources.id                      AS owned_data_source_id
         FROM studies_data_sources
                  JOIN data_sources ON data_sources.id = studies_data_sources.data_source_id
                  JOIN datanodes ON datanodes.id = data_sources.data_node_id
                  JOIN datanodes_users ON datanodes.id = datanodes_users.datanode_id
                  LEFT JOIN studies_data_sources_comment
                            ON studies_data_sources_comment.studies_data_sources_id = studies_data_sources.id
                                AND datanodes_users.user_id = studies_data_sources_comment.user_id
     ) AS data;