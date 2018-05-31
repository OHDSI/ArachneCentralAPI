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
 * Created: December 09, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

public enum SubmissionStatus {
    PENDING("PENDING EXECUTION"),
    NOT_APPROVED("REJECTED"),
    STARTING("IN QUEUE"),
    QUEUE_PROCESSING("QUEUE PROCESSING"),
    IN_PROGRESS("IN PROGRESS"),
    EXECUTED("AWAITING APPROVAL (SUCCESS)"),
    FAILED("AWAITING APPROVAL (FAILED)"),
    EXECUTED_REJECTED("REJECTED"),
    FAILED_REJECTED("REJECTED"),
    EXECUTED_PUBLISHED("FINISHED"),
    FAILED_PUBLISHED("FAILED");

    private String title;

    SubmissionStatus(String title) {

        this.title = title;
    }

    @Override
    public String toString() {

        return title;
    }

    public boolean isPublished() {

        return EXECUTED_PUBLISHED == this || FAILED_PUBLISHED == this;
    }

    public boolean isFinished() {

        return EXECUTED == this || FAILED == this;
    }

    public boolean isDeclined() {

        return NOT_APPROVED == this || EXECUTED_REJECTED == this || FAILED_REJECTED == this;
    }

}