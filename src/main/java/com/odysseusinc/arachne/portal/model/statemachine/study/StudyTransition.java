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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyStatus;
import com.odysseusinc.arachne.portal.model.statemachine.Transition;
import java.util.function.Predicate;

public class StudyTransition implements Transition<Study, StudyStatus> {

    public StudyTransition(StudyStatus to, StudyStatus from, String description, boolean isInfo, Predicate<Study> discriminator) {

        if (to == null || from == null || description == null) {
            throw new IllegalArgumentException("TO, FROM, DESCRIPTION params have to be set");
        }

        this.to = to;
        this.from = from;
        this.isInfo = isInfo;
        this.description = description;
        this.discriminator = discriminator;
    }

    public StudyTransition(StudyTransition from) {
        this(from.to, from.from, from.description, from.isInfo, from.discriminator);
    }

    private boolean isInfo;
    private final StudyStatus to;
    private final StudyStatus from;
    private final String description;
    private Predicate<Study> discriminator = (study) -> Boolean.FALSE;

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public StudyStatus getTo() {
        return this.to;
    }

    @Override
    public StudyStatus getFrom() {
        return this.from;
    }

    @Override
    public boolean isInfo() {
        return this.isInfo;
    }

    public void setInfo() {

        this.isInfo = Boolean.TRUE;
    }

    @Override
    @JsonIgnore
    public StudyTransition getCopy() {
        return new StudyTransition(this);
    }

    @Override
    public Predicate<Study> getDiscriminator() {

        return this.discriminator;
    }

    public void setDiscriminator(Predicate<Study> discriminator) {

        this.discriminator = discriminator;
    }

    @Override
    public String toString() {

        return "StudyTransition{" +
                "isInfo=" + isInfo +
                ", from=" + from +
                ", to=" + to +
                '}';
    }

    public static final class StudyTransitionBuilder {
        private boolean isInfo = Boolean.FALSE;
        private StudyStatus to;
        private StudyStatus from;
        private String description;
        private Predicate<Study> discriminator = (study) -> Boolean.FALSE;

        private StudyTransitionBuilder() {
        }

        public static StudyTransitionBuilder b() {
            return new StudyTransitionBuilder();
        }

        public StudyTransitionBuilder info() {
            this.isInfo = Boolean.TRUE;
            return this;
        }

        public StudyTransitionBuilder to(StudyStatus to) {
            this.to = to;
            return this;
        }

        public StudyTransitionBuilder from(StudyStatus from) {
            this.from = from;
            return this;
        }

        public StudyTransitionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public StudyTransitionBuilder discriminator(Predicate<Study> discriminator) {
            this.discriminator = discriminator;
            return this;
        }

        public StudyTransition build() {
            return new StudyTransition(to, from, description, isInfo, discriminator);
        }
    }
}
