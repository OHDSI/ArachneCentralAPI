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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: January 22, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AntivirusFile {
    @Column
    @Enumerated(EnumType.STRING)
    protected AntivirusStatus antivirusStatus = AntivirusStatus.SCANNING;
    @Column
    protected String antivirusDescription;

    public AntivirusStatus getAntivirusStatus() {

        return antivirusStatus;
    }

    public void setAntivirusStatus(AntivirusStatus antivirusStatus) {

        this.antivirusStatus = antivirusStatus;
    }

    public String getAntivirusDescription() {

        return antivirusDescription;
    }

    public void setAntivirusDescription(String antivirusDescription) {

        this.antivirusDescription = antivirusDescription;
    }
}
