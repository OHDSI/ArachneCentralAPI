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
 * Created: February 15, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.portal.model.SubmissionStatus;

public class SubmissionStatusDTO {
    protected SubmissionStatus value;
    protected String title;

    public SubmissionStatusDTO() {

    }

    public SubmissionStatusDTO(SubmissionStatus status) {

        this.value = status;
        this.title = status.toString();
    }

    public SubmissionStatusDTO(SubmissionStatusDTO status) {

        if (status != null) {
            this.value = status.value;
            this.title = status.title;
        }
    }

    public SubmissionStatus getValue() {

        return value;
    }

    public void setValue(SubmissionStatus value) {

        this.value = value;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }
}
