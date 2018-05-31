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
 * Created: August 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.statemachine.study;

import com.odysseusinc.arachne.portal.model.PublishState;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyStatus;
import com.odysseusinc.arachne.portal.model.statemachine.ObjectRepository;
import com.odysseusinc.arachne.portal.model.statemachine.RepositoryBasedStateMachine;
import com.odysseusinc.arachne.portal.model.statemachine.StateRepository;

import java.util.Arrays;
import java.util.function.Predicate;
import javax.annotation.PostConstruct;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("flywayInitializer")
public class StudyStateMachine extends RepositoryBasedStateMachine<Study, StudyStatus, StudyTransition> {

    //TODO find a better way of doing this, but because of lack of time it's postponed
    private final String INITIATE = StudyState.INITIATE.getStateName();
    private final String ACTIVE = StudyState.ACTIVE.getStateName();
    private final String COMPLETED = StudyState.COMPLETED.getStateName();
    private final String ARCHIVED = StudyState.ARCHIVED.getStateName();

    public StudyStateMachine(ObjectRepository<Study> objectRepository, StateRepository<StudyStatus> statusRepository) {

        super(objectRepository, statusRepository);
    }

    @PostConstruct
    private void configure() {

        reconfigure();
    }

    @Override
    public void reconfigure() {

        loadStates();

        verifyStates(Arrays.asList(INITIATE, ACTIVE, COMPLETED, ARCHIVED));

        Predicate<Study> isAnalysisListEmpty = study -> study.getAnalyses().isEmpty();

        createTransition(
                StudyTransition.StudyTransitionBuilder.b()
                        .from(getStateByName(INITIATE))
                        .to(getStateByName(ACTIVE))
                        .description("Create an analysis")
                        .discriminator(isAnalysisListEmpty)
                        .build());

        createTransition(
                StudyTransition.StudyTransitionBuilder.b()
                        .from(getStateByName(ACTIVE))
                        .to(getStateByName(INITIATE))
                        .description("Delete all analyses")
                        .discriminator(isAnalysisListEmpty.negate())
                        .build());

        createTransition(
                StudyTransition.StudyTransitionBuilder.b()
                        .from(getStateByName(ACTIVE))
                        .to(getStateByName(COMPLETED))
                        .description("")
                        .build());

        createTransition(
                StudyTransition.StudyTransitionBuilder.b()
                        .from(getStateByName(COMPLETED))
                        .to(getStateByName(ACTIVE))
                        .description("Paper already published")
                        .discriminator((study) -> study.getPaper() != null
                                && study.getPaper().getPublishState() == PublishState.PUBLISHED)
                        .build());

        createTransition(
                StudyTransition.StudyTransitionBuilder.b()
                        .from(getStateByName(ARCHIVED))
                        .to(getStateByName(COMPLETED))
                        .description("")
                        .build());

        createTransition(
                StudyTransition.StudyTransitionBuilder.b()
                        .from(getStateByName(COMPLETED))
                        .to(getStateByName(ARCHIVED))
                        .description("")
                        .build());
    }
}
