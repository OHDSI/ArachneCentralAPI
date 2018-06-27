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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Study;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseAnalysisRepository<T extends Analysis> extends EntityGraphJpaRepository<T, Long> {
    List<T> findByTitleAndStudyId(String title, Long studyId);

    List<T> findByStudyOrderByOrd(Study study);

    Optional<T> findByIdAndAndLockedTrue(Long id);

    @Query("select max(a.ord) from Analysis a where a.study.id=:id")
    Integer getMaxOrd(@Param("id") Long id);

    List<T> findByStudyIdIn(List<Long> ids);

    void deleteByIdIn(List<Long> ids);

    T findById(Long id, EntityGraph entityGraph);

    T findById(Long id);

    List<T> findByIdIn(List<Long> ids);

    List<T> findByStudyId(Long studyId, EntityGraph entityGraph);
}
