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
 * Created: May 23, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.achilles;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.springframework.util.AntPathMatcher;

@Entity
@Table(name = "achilles_report_matchers")
public class AchillesReportMatcher {
    @Id
    @SequenceGenerator(name = "achilles_report_matcher_pk_sequence",
            sequenceName = "achilles_report_matchers_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "achilles_report_matcher_pk_sequence")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achilles_report_id")
    private AchillesReport report;
    @Column
    private String pattern;
    @Transient
    private AntPathMatcher matcher = new AntPathMatcher();

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public AchillesReport getReport() {

        return report;
    }

    public void setReport(AchillesReport report) {

        this.report = report;
    }

    public String getPattern() {

        return pattern;
    }

    public void setPattern(String pattern) {

        this.pattern = pattern;
    }

    public boolean match(AchillesFile file) {

        return matcher.match(pattern, file.getFilePath());
    }
}
