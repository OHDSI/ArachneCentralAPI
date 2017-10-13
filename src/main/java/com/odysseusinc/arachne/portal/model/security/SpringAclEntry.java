/**
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Created: January 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "acl_entry")
public class SpringAclEntry {

    @Id
    @SequenceGenerator(name = "acl_entry_pk_sequence", sequenceName = "acl_entry_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acl_entry_pk_sequence")
    private Long id;

    @Column
    private Long aclObjectIdentity;

    @Column
    private Integer aceOrder;

    @Column
    private Long sid;

    @Column
    private Integer mask;

    @Column
    private Boolean granting;

    @Column
    private Boolean auditSuccess;

    @Column
    private Boolean auditFailure;


    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getAclObjectIdentity() {

        return aclObjectIdentity;
    }

    public void setAclObjectIdentity(Long aclObjectIdentity) {

        this.aclObjectIdentity = aclObjectIdentity;
    }

    public Integer getAceOrder() {

        return aceOrder;
    }

    public void setAceOrder(Integer aceOrder) {

        this.aceOrder = aceOrder;
    }

    public Long getSid() {

        return sid;
    }

    public void setSid(Long sid) {

        this.sid = sid;
    }

    public Integer getMask() {

        return mask;
    }

    public void setMask(Integer mask) {

        this.mask = mask;
    }

    public Boolean getGranting() {

        return granting;
    }

    public void setGranting(Boolean granting) {

        this.granting = granting;
    }

    public Boolean getAuditSuccess() {

        return auditSuccess;
    }

    public void setAuditSuccess(Boolean auditSuccess) {

        this.auditSuccess = auditSuccess;
    }

    public Boolean getAuditFailure() {

        return auditFailure;
    }

    public void setAuditFailure(Boolean auditFailure) {

        this.auditFailure = auditFailure;
    }

}
