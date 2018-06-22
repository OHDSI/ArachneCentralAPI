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
 * Created: January 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

public interface InvitationType {
    String COLLABORATOR = "COLLABORATOR";
    String DATA_OWNER = "DATA_OWNER";
    String UNLOCK_ANALYSIS = "UNLOCK_ANALYSIS";
    String APPROVE_EXECUTE_SUBMISSION = "APPROVE_EXECUTE_SUBMISSION";
    String APPROVE_PUBLISH_SUBMISSION = "APPROVE_PUBLISH_SUBMISSION";
}
