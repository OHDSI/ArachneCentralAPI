/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Created: September 08, 2017
 *
 */
package com.odysseusinc.arachne.portal.service.study.impl;

import com.odysseusinc.arachne.portal.api.v1.dto.UpdateNotificationDTO;
import com.odysseusinc.arachne.portal.config.WebSecurityConfig;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.repository.StudyDataSourceLinkRepository;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.mail.InvitationDataOwnerMailSender;
import com.odysseusinc.arachne.portal.service.study.AddDataSourceStrategy;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.odysseusinc.arachne.portal.model.DataSourceStatus.APPROVED;
import static com.odysseusinc.arachne.portal.model.DataSourceStatus.PENDING;

@Service
public class AddRestrictedDataSourceStrategy extends AbstractAddDataSourceStrategy implements AddDataSourceStrategy<IDataSource> {

    private final ArachneMailSender arachneMailSender;
    private final SimpMessagingTemplate wsTemplate;

    public AddRestrictedDataSourceStrategy(StudyDataSourceLinkRepository studyDataSourceLinkRepository,
                                           ArachneMailSender arachneMailSender,
                                           SimpMessagingTemplate wsTemplate) {

        super(studyDataSourceLinkRepository);
        this.arachneMailSender = arachneMailSender;
        this.wsTemplate = wsTemplate;
    }

    @Override
    public void addDataSourceToStudy(IUser createdBy, IDataSource dataSource, StudyDataSourceLink link) {

        DataNode dataNode = dataSource.getDataNode();
        if (DataNodeUtils.isDataNodeOwner(dataNode, createdBy)) {
            saveStudyDataSourceLinkWithStatus(link, APPROVED);
        } else {
            StudyDataSourceLink studyDataSourceLink
                    = saveStudyDataSourceLinkWithStatus(link, PENDING);
            DataNodeUtils.getDataNodeOwners(dataNode).forEach(
                    user -> {
                        arachneMailSender.send(new InvitationDataOwnerMailSender(
                                        WebSecurityConfig.getDefaultPortalURI(), user, studyDataSourceLink
                                )
                        );
                        wsTemplate.convertAndSendToUser(user.getUsername(), "/topic/invitations",
                                new UpdateNotificationDTO());
                    }
            );
        }
    }
}
