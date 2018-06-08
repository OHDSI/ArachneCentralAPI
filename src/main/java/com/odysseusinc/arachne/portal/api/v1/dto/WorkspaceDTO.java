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
 * Authors: Anastasiia Klochkova
 * Created: May 30, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

public class WorkspaceDTO extends CommonStudyDTO {

    private ParticipantDTO leadParticipant;

    public ParticipantDTO getLeadParticipant() {

        return leadParticipant;
    }

    public void setLeadParticipant(ParticipantDTO leadParticipant) {

        this.leadParticipant = leadParticipant;
    }
}
