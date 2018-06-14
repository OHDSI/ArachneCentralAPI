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
 * Created: July 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.api.v1.dto.converters.PaperSolrExtractors;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.model.solr.SolrEntity;
import com.odysseusinc.arachne.portal.model.solr.SolrFieldAnno;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.DiscriminatorFormula;
import org.springframework.util.CollectionUtils;

@Entity
@Table(name = "papers")
@DiscriminatorFormula("'PAPER_ENTITY'")
@SolrFieldAnno(name = BaseSolrService.TITLE, postfix = false, extractor = PaperSolrExtractors.TitleExtractor.class)
@SolrFieldAnno(extractor = PaperSolrExtractors.TitleExtractor.class, name = "study_title", query = true, filter = true)
public class Paper implements HasArachnePermissions, Breadcrumb, SolrEntity {

    @Id
    @SequenceGenerator(name = "papers_pk_sequence", sequenceName = "papers_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "papers_pk_sequence")
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    @SolrFieldAnno(extractor = PaperSolrExtractors.StudyIdExtractor.class, sort = false, name = "study_id")
    private Study study;

    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaperProtocolFile> protocols = new ArrayList<>();

    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaperPaperFile> papers = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "paper_favourites",
            joinColumns = @JoinColumn(name = "paper_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private List<RawUser> followers = new ArrayList<>();

    @Column
    @Enumerated(EnumType.STRING)
    private PublishState publishState;

    @Column
    private Date publishedDate;

    @Transient
    private Set<ArachnePermission> permissions = new HashSet<>();

    @Override
    public Long getId() {

        return id;
    }

    @Override
    public String getCrumbTitle() {

        return getStudy().getTitle();
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Study getStudy() {

        return study;
    }

    public void setStudy(Study study) {

        this.study = study;
    }

    public List<PaperProtocolFile> getProtocols() {

        return protocols;
    }

    public void setProtocols(List<PaperProtocolFile> protocol) {

        this.protocols = protocol;
    }

    public List<PaperPaperFile> getPapers() {

        return papers;
    }

    public void setPapers(List<PaperPaperFile> paper) {

        this.papers = paper;
    }

    public PublishState getPublishState() {

        return publishState;
    }

    public void setPublishState(PublishState publishState) {

        this.publishState = publishState;
    }

    public Date getPublishedDate() {

        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {

        this.publishedDate = publishedDate;
    }

    @Override
    public BreadcrumbType getCrumbType() {

        return BreadcrumbType.PAPER;
    }

    @Override
    public Set<ArachnePermission> getPermissions() {

        return permissions;
    }

    @Override
    public void setPermissions(Set<ArachnePermission> permissions) {

        if (!CollectionUtils.isEmpty(permissions)) {
            this.permissions = permissions;
        }
    }

    @Override
    public Breadcrumb getCrumbParent() {

        return getStudy();
    }

    public List<RawUser> getFollowers() {

        return followers;
    }

    public void setFollowers(List<RawUser> followers) {

        this.followers = followers;
    }

    @Override
    public SolrCollection getCollection() {

        return SolrCollection.PAPERS;
    }
}
