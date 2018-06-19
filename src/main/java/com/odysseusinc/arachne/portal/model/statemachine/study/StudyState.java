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

import static com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateActions.CREATE_PAPER;
import static com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateActions.PUBLISH_PAPER;

public enum StudyState {

    INITIATE("Initiate", new StudyStateActions[]{}),
    ACTIVE("Active", new StudyStateActions[]{ CREATE_PAPER }),
    COMPLETED("Completed", new StudyStateActions[]{ CREATE_PAPER, PUBLISH_PAPER }),
    ARCHIVED("Archived", new StudyStateActions[]{ CREATE_PAPER, PUBLISH_PAPER });

    private String stateName;
    private StudyStateActions[] actions;

    StudyState(String stateName, StudyStateActions[] actions) {
        this.stateName = stateName;
        this.actions = actions;
    }

    public String getStateName() {

        return stateName;
    }

    public StudyStateActions[] getActions() {

        return actions;
    }
}
