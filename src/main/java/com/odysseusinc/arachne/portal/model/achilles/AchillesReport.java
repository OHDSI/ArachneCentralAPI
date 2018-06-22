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

import com.google.common.base.Objects;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "achilles_reports")
public class AchillesReport {
    @Id
    @SequenceGenerator(name = "achilles_report_pk_sequence", sequenceName = "achilles_reports_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "achilles_report_pk_sequence")
    private Long id;
    @Column(name = "label", nullable = false)
    private String label;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    @OneToMany(targetEntity = AchillesReportMatcher.class, mappedBy = "report")
    private List<AchillesReportMatcher> matchers;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public List<AchillesReportMatcher> getMatchers() {

        return matchers;
    }

    public void setMatchers(List<AchillesReportMatcher> matchers) {

        this.matchers = matchers;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    public Integer getSortOrder() {

        return sortOrder;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AchillesReport report = (AchillesReport) o;
        return Objects.equal(id, report.id)
                && Objects.equal(label, report.label);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(id, label);
    }
}
