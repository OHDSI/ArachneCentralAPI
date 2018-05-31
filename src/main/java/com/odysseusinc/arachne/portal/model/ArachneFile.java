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
 * Created: May 10, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import java.util.Date;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
@Access(AccessType.FIELD)
public class ArachneFile extends AntivirusFile implements ArachneFileMeta, HasArachnePermissions {

    @Column
    protected String uuid;
    @Column
    protected String label;
    @Column
    protected String realName;
    @Column
    protected String contentType;
    @Column
    protected Date created;
    @Column
    protected Date updated;

    @Transient
    private Set<ArachnePermission> permissions;

    public ArachneFile() {

    }

    public ArachneFile(String uuid, String label, String realName, String contentType, Date created, Date updated) {

        this.uuid = uuid;
        this.label = label;
        this.realName = realName;
        this.contentType = contentType;
        this.created = created;
        this.updated = updated;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    @Deprecated
    public String getRealName() {

        return realName;
    }

    public String getName() {

        return realName;
    }

    public void setRealName(String realName) {

        this.realName = realName;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Date getUpdated() {

        return updated;
    }

    public void setUpdated(Date updated) {

        this.updated = updated;
    }

    public String getContentType() {

        return contentType;
    }

    public void setContentType(String contentType) {

        this.contentType = contentType;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    @Override
    public Set<ArachnePermission> getPermissions() {

        return this.permissions;
    }

    @Override
    public void setPermissions(Set<ArachnePermission> permissions) {

        this.permissions = permissions;
    }
}
