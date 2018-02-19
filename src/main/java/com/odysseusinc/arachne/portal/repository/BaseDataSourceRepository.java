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
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.IDataSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseDataSourceRepository<T extends IDataSource> extends CrudRepository<T, Long> {

    T findOne(Long id);

    List<T> findByIdInAndDeletedIsNull(List<Long> ids);

    Optional<T> findByName(String name);

    T findByUuid(String uuid);

    Optional<T> findByIdAndDeletedIsNull(Long id);

    @Query(value = "SELECT *"
            + " FROM data_sources AS ds "
            + " JOIN datanodes_users AS u "
            + "   ON ds.data_node_id=u.datanode_id "
            + " WHERE ds.deleted IS NULL "
            + " AND u.user_id=:userId ",
            nativeQuery = true)
    List<T> getAllByUserId(@Param("userId") Long userId);

    @Query(nativeQuery = true, value = "SELECT DISTINCT ON (ds.name) * FROM data_sources AS ds "
            + " JOIN datanodes_users AS dnu ON ds.data_node_id=dnu.datanode_id "
            + " JOIN datanodes AS dn ON ds.data_node_id=dn.id "
            + " WHERE "
            + " ds.id NOT IN (SELECT data_source_id FROM studies_data_sources WHERE study_id=:studyId "
            + "            AND lower(status) NOT IN ('deleted', 'declined'))"
            + " AND lower(ds.name || ' ' || dn.name) SIMILAR TO :suggestRequest "
            + " AND ds.deleted IS NULL "
            + " AND dn.is_virtual = FALSE \n"
            + " \n--#pageable\n",
            countQuery = "SELECT COUNT(*) FROM (SELECT DISTINCT ON (ds.name) * FROM data_sources AS ds "
                    + " JOIN datanodes_users AS dnu ON ds.data_node_id=dnu.datanode_id "
                    + " JOIN datanodes AS dn ON ds.data_node_id=dn.id "
                    + " WHERE "
                    + " ds.id NOT IN (SELECT data_source_id FROM studies_data_sources WHERE study_id=:studyId "
                    + "            AND lower(status) NOT IN ('deleted', 'declined'))"
                    + " AND lower(ds.name || ' ' || dn.name) SIMILAR TO :suggestRequest "
                    + " AND ds.deleted IS NULL "
                    + " AND dn.is_virtual = FALSE) as innerSelect"
           )
    Page<T> suggest(@Param("suggestRequest") String suggestRequest, @Param("studyId") Long studyId,
                             Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT * FROM data_sources ds JOIN characterizations c ON c.datasource_id = ds.id "
            + "JOIN achilles_files f ON f.characterization_id = c.id WHERE f.id = :fileId")
    T getByAchillesFileId(@Param("fileId") Long fileId);

    List<T> getByDataNodeVirtualAndDeletedIsNull(Boolean isVirtual);

    // Have to do native query because of request to non-tenant table w/ data sources
    @Query(
            nativeQuery = true,
            value = "SELECT * " +
                    "FROM data_sources_data ds " +
                    "JOIN datanodes dn ON dn.id = ds.data_node_id " +
                    "WHERE dn.is_virtual = FALSE " +
                    "AND ds.deleted IS NULL")
    List<T> getAllNotDeletedAndIsNotVirtualFromAllTenants();

    @Transactional
    int deleteByIdAndDeletedIsNull(Long id);

    List<T> findByIdIn(List<Long> ids);
}
