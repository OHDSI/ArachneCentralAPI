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
 * Created: July 21, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import com.odysseusinc.arachne.portal.api.v1.dto.DataNodeDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.OrganizationDTO;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DataNodeToDataNodeDTOConverter extends BaseConversionServiceAwareConverter<DataNode, DataNodeDTO> {

    @Override
    public DataNodeDTO convert(DataNode dataNode) {

        final DataNodeDTO dataNodeDTO = new DataNodeDTO();
        dataNodeDTO.setId(dataNode.getId());
        dataNodeDTO.setName(dataNode.getName());
        dataNodeDTO.setDescription(dataNode.getDescription());
        dataNodeDTO.setVirtual(dataNode.getVirtual());
        dataNodeDTO.setPublished(dataNode.getPublished());
        dataNodeDTO.setAtlasVerion(dataNode.getAtlasVersion());
        CommonHealthStatus healthStatus = dataNode.getHealthStatus();
        dataNodeDTO.setHealthStatus(healthStatus);
        dataNodeDTO.setHealthStatusTitle(healthStatus.toString());
        final Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        final Long loggedUserId = ((ArachneUser) principal).getId();
        final User loggedUser = new User();
        loggedUser.setId(loggedUserId);
        dataNodeDTO.setCurrentUserDataOwner(DataNodeUtils.isDataNodeOwner(dataNode, loggedUser));
        final OrganizationDTO organizationDTO = conversionService.convert(dataNode.getOrganization(), OrganizationDTO.class);
        dataNodeDTO.setOrganization(organizationDTO);
        return dataNodeDTO;
    }
}
