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
 * Created: October 19, 2016
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.Skill;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SkillRepository<S extends Skill> extends JpaRepository<S, Long> {

    List<S> findByName(String name);

    @Query(nativeQuery = true,
            value = "SELECT * FROM skills  WHERE lower(name) SIMILAR TO :suggestRequest LIMIT :limit")
    List<S> suggest(@Param("suggestRequest") String suggestRequest, @Param("limit") Integer limit);

    @Query("SELECT skill FROM Skill skill " +
            "WHERE skill.id NOT IN" +
            "      (SELECT skill.id FROM User u" +
            "       JOIN u.skills skill" +
            "      WHERE u.id=:userId)")
    List<S> getAllExpectOfUserSkills(@Param("userId") Long userId);
}
