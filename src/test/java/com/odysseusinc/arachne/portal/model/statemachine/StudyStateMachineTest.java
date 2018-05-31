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
 * Created: August 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.statemachine;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyStatus;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyState;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateMachine;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyTransition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class StudyStateMachineTest {

    private static final String INITIATE = StudyState.INITIATE.getStateName();
    private static final String ACTIVE = StudyState.ACTIVE.getStateName();
    private static final String COMPLETED = StudyState.COMPLETED.getStateName();
    private static final String ARCHIVED = StudyState.ARCHIVED.getStateName();

    private StudyStateMachine stateMachine = new StudyStateMachine(new StudyRepo(), new StudyStatusRepo());

    private static List<String> statuses = Arrays.asList(INITIATE, ACTIVE, COMPLETED, ARCHIVED);

    {
        stateMachine.reconfigure();
        stateMachine.verifyStates(statuses);
    }

    @Test
    public void testMoveToState() {

        Study study = createStudy(ACTIVE);

        study = stateMachine.moveToState(study, COMPLETED);

        assertEquals(COMPLETED, study.getStatus().getName());
    }

    @Test(expected = IllegalStateMoveException.class)
    public void testMoveToIllegalState() {

        Study study = createStudy(INITIATE);

        stateMachine.moveToState(study, ARCHIVED);
    }

    @Test
    public void testAvailableStates() {

        Study study = createStudy(INITIATE);

        List<StudyTransition> transitionsFrom = stateMachine.getTransitionsFrom(study.getStatus());

        assertEquals(1, transitionsFrom.size());
        assertEquals(transitionsFrom.get(0).getFrom().getName(), INITIATE);
        assertEquals(transitionsFrom.get(0).getTo().getName(), ACTIVE);
    }

    @Test
    public void testAnalysisListDiscriminatorDoingNothing() {

        Study activeStudy = createStudy(ACTIVE);

        List<StudyTransition> availableStates = stateMachine.getAvailableStates(activeStudy);

        assertEquals(2, availableStates.size());
        assertFalse(availableStates.get(0).isInfo());
        assertFalse(availableStates.get(1).isInfo());
    }

    @Test
    public void testAnalysisListDiscriminatorChangingInfo() {

        Study activeStudy = createStudy(ACTIVE);
        activeStudy.getAnalyses().add(new Analysis());

        //now study has one analysis and transitions from Active to Initiate should have Info status

        List<StudyTransition> availableStates = stateMachine.getAvailableStates(activeStudy);

        assertEquals(2, availableStates.size());
        assertTrue(availableStates.get(0).isInfo());
        assertFalse(availableStates.get(1).isInfo());
    }

    private Study createStudy(String statusName) {

        StudyStatus status = stateMachine.getStateByName(statusName);

        Study study = new Study();
        study.setStatus(status);

        return study;
    }

    private class StudyRepo implements ObjectRepository<Study> {

        @Override
        public <S extends Study> S save(S var1) {

            return var1;
        }
    }

    private class StudyStatusRepo implements StateRepository<StudyStatus> {

        private List<StudyStatus> repoStatuses = new ArrayList<>(4);

        {
            statuses.forEach((statusString) -> {
                StudyStatus status = new StudyStatus();
                status.setId((long) statuses.indexOf(statusString));
                status.setName(statusString);
                repoStatuses.add(status);
            });
        }

        @Override
        public List<StudyStatus> findAll() {

            return this.repoStatuses;
        }

        @Override
        public StudyStatus findByName(String stateName) {

            return this.repoStatuses.stream().filter(status -> Objects.equals(stateName, status.getName())).findFirst().orElseGet(null);
        }
    }

}