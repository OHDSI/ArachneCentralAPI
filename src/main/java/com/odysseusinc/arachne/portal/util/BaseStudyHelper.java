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
 * Created: September 06, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import static com.odysseusinc.arachne.commons.api.v1.dto.CommonModelType.CDM;
import static com.odysseusinc.arachne.portal.service.AnalysisPaths.CONTENT_DIR;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonCDMVersionDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.security.DataNodeAuthenticationToken;
import com.odysseusinc.arachne.portal.security.Roles;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseStudyHelper<DN extends DataNode, DS extends IDataSource> {

    private final Logger LOGGER = LoggerFactory.getLogger(BaseStudyHelper.class);

    private static final String VIRTUAL_DATANODE_NAME = "Virtual";
    private static final String VIRTUAL_DATANODE_HEALTH_STATUS_DESC = "Virtual DataNodes are always GREEN";
    private static final String VIRTUAL_DATANODE_DESC = "Virtual Node for Study=%s and DataSource=%s";
    @Value("${files.store.path}")
    private String fileStorePath;

    public DN getVirtualDataNode(final String studyTitle, final String dataSourceName) {

        final DN dataNode = createDataNode();
        dataNode.setVirtual(true);
        dataNode.setName(VIRTUAL_DATANODE_NAME);
        final String description = String.format(VIRTUAL_DATANODE_DESC, studyTitle, dataSourceName);
        dataNode.setDescription(description);
        dataNode.setPublished(true);
        dataNode.setHealthStatus(CommonHealthStatus.GREEN);
        dataNode.setHealthStatusDescription(VIRTUAL_DATANODE_HEALTH_STATUS_DESC);
        return dataNode;
    }

    public Set<DataNodeUser> createDataNodeUsers(final List<IUser> users, final DN dataNode) {

        return users.stream()
                .map(u -> new DataNodeUser(u, dataNode))
                .collect(Collectors.toSet());
    }

    public Authentication loginByNode(DN dataNode) {

        final Authentication savedAuth = SecurityContextHolder.getContext().getAuthentication();
        Collection<GrantedAuthority> authorities
                = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + Roles.ROLE_DATA_NODE));
        DataNodeAuthenticationToken dataNodeAuth
                = new DataNodeAuthenticationToken(dataNode.getToken(), dataNode, authorities);
        SecurityContextHolder.getContext().setAuthentication(dataNodeAuth);
        return savedAuth;
    }

    public DS getVirtualDataSource(final DN dataNode, final String name) {

        final DS dataSource = createDataSource();
        dataSource.setDataNode(dataNode);
        dataSource.setName(name);
        dataSource.setModelType(CDM);
        dataSource.setCreated(new Date());
        dataSource.setCdmVersion(CommonCDMVersionDTO.V4_0);
        return dataSource;
    }

    public Path getStudyFolder(Study study) {

        String content = fileStorePath + File.separator + CONTENT_DIR;
        return Paths.get(content, Objects.toString(study.getId()));
    }

    public void tryDeleteStudyFolder(Study study) {

        File studyFolder = getStudyFolder(study).toFile();
        try {
            FileUtils.deleteDirectory(studyFolder);
        } catch (IOException e) {
            LOGGER.info("Unsuccessful attempt to delete study folder {}", studyFolder.toString());
        }
    }

    protected abstract DS createDataSource();

    protected abstract DN createDataNode();
}
