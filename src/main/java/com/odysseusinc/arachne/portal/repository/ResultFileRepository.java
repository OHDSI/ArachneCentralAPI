/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Created: December 06, 2016
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.model.ResultFile;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ResultFileRepository extends CrudRepository<ResultFile, Long> {

    ResultFile findByUuid(String uuid);

    @Query(
         value = "WITH files AS (" +
                 "  SELECT DISTINCT ON (split_part(substr(real_name, LENGTH(:path) + 1), '/', 1))" +
                 "    id, " +
                 "    submission_id, " +
                 "    uuid, " +
                 "    split_part(substr(real_name, LENGTH(:path) + 1), '/', 1) real_name, " +
                 "    created, " +
                 "    updated, " +
                 "    mime_type, " +
                 "    label, " +
                 "    comment_topic_id, " +
                 "    manual_upload, " +
                 "    CASE WHEN substr(real_name, LENGTH(:path) + 1) ~ '/' THEN '" + CommonFileUtils.TYPE_FOLDER + "' ELSE content_type END content_type " +
                 "  FROM result_files " +
                 "  WHERE submission_id = :submissionId " +
                 "  AND ((:path = '') OR (:path <> '' AND real_name LIKE :#{#path + '%'}))" +
                 ") " +
                 "SELECT *" +
                 "FROM files " +
                 "ORDER BY " +
                 "  CASE WHEN content_type = '" + CommonFileUtils.TYPE_FOLDER + "' " +
                 "    THEN '0' || split_part(substr(real_name, LENGTH(:path) + 1), '/', 1) " +
                 "    ELSE '1' || real_name " +
                 "  END",
            nativeQuery = true
    )
    List<ResultFile> findBySubmissionAndPath(@Param("submissionId") Long submissionId, @Param("path") String path);
}
