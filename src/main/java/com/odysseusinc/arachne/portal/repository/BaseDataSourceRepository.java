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
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.odysseusinc.arachne.portal.model.IDataSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface BaseDataSourceRepository<T extends IDataSource> extends EntityGraphJpaRepository<T, Long> {

    String USERS_DATASOURCES_QUERY = "SELECT * FROM data_sources_data AS ds\n" +
            " JOIN datanodes_users AS dnu ON ds.data_node_id=dnu.datanode_id\n" +
            " JOIN datanodes AS dn ON ds.data_node_id=dn.id\n" +
            " WHERE\n" +
            " lower(ds.name) SIMILAR TO :suggestRequest\n" +
            " AND dnu.user_id = :userId\n" +
            " AND ds.deleted IS NULL\n" +
            " AND dn.is_virtual = FALSE\n";

    T findOne(Long id);

    List<T> findByIdInAndDeletedIsNullAndPublishedTrue(List<Long> ids);

    Optional<T> findByName(String name);

    T findByUuid(String uuid);

    Optional<T> findByIdAndDeletedIsNull(Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM data_sources ds JOIN characterizations c ON c.datasource_id = ds.id "
            + "JOIN achilles_files f ON f.characterization_id = c.id WHERE f.id = :fileId")
    T getByAchillesFileId(@Param("fileId") Long fileId);

    // Have to do native query because of request to non-tenant table w/ data sources
    @Query(
            nativeQuery = true,
            value = "SELECT * " +
                    "FROM data_sources_data ds " +
                    "JOIN datanodes dn ON dn.id = ds.data_node_id " +
                    "WHERE dn.is_virtual = FALSE " +
                    "AND ds.deleted IS NULL AND ds.published = TRUE AND :withManual")
    List<T> getAllNotDeletedAndIsNotVirtualAndPublishedTrueFromAllTenants(@Param("withManual") boolean withManual);

    @Transactional
    int deleteByIdAndDeletedIsNull(Long id);

    List<T> findByIdIn(List<Long> ids);

    @Query(nativeQuery = true, value = USERS_DATASOURCES_QUERY +"\n"
            + " \n--#pageable\n",
            countQuery = "SELECT COUNT(*) FROM (" + USERS_DATASOURCES_QUERY + ") as innerSelect"
    )
    Page<T> getUserDataSources(@Param("suggestRequest") String suggestRequest, @Param("userId") Long userId, Pageable pageable);
}
