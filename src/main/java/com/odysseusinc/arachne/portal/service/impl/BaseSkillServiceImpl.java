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
 * Created: September 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;

import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.repository.SkillRepository;
import com.odysseusinc.arachne.portal.service.BaseSkillService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public abstract class BaseSkillServiceImpl<S extends Skill> extends CRUDLServiceImpl<S> implements BaseSkillService<S> {

    private final SkillRepository<S> skillRepository;

    @Autowired
    protected BaseSkillServiceImpl(SkillRepository<S> skillRepository) {

        this.skillRepository = skillRepository;
    }

    @Override
    public CrudRepository<S, Long> getRepository() {

        return this.skillRepository;
    }

    @Override
    public S create(S skill) throws NotUniqueException {

        List<S> skills = skillRepository.findByName(skill.getName());
        if (!skills.isEmpty()) {
            throw new NotUniqueException("name", "Not unique");
        }
        return skillRepository.save(skill);
    }

    @Override
    public S update(S skill) throws NotUniqueException, NotExistException {

        if (!skillRepository.exists(skill.getId())) {
            throw new NotExistException("update: skill with id=" + skill.getId() + " not exist", getType());
        }
        List<S> skills = skillRepository.findByName(skill.getName());
        if (!skills.isEmpty()) {
            throw new NotUniqueException("name", "Not unique");
        }
        return skillRepository.save(skill);
    }

    public abstract Class<S> getType();

    @Override
    public List<S> suggestSkill(final String query, final Integer limit) {

        String[] split = query.trim().split(" ");
        StringBuilder suggestRequest = new StringBuilder("%(");
        for (String s : split) {
            suggestRequest.append(s.toLowerCase()).append("|");
        }
        suggestRequest.delete(suggestRequest.length() - 1, suggestRequest.length());
        suggestRequest.append(")%");
        return skillRepository.suggest(suggestRequest.toString(), limit);
    }

    @Override
    public List<S> getAllExpectOfUserSkills(Long userId) {

        return skillRepository.getAllExpectOfUserSkills(userId);
    }

    @Override
    public List<S> findAll() {

        return skillRepository.findAll();
    }
}
