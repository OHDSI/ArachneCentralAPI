/*
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
 * Created: September 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeRole;
import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BaseDataNodeService<DN extends DataNode> {
    DN create(DN dataNode);

    DN update(DN dataNode) throws NotExistException;

    DN updateAtlasInfo(DN dataNode) throws NotExistException;

    List<DN> findAllIsNotVirtual();

    DN getById(Long id) throws NotExistException;

    void linkUserToDataNode(DN dataNode, User user, Set<DataNodeRole> dataNodeRole)
            throws NotExistException, AlreadyExistException;

    void unlinkUserToDataNode(DN datanode, User user) throws NotExistException;

    void relinkAllUsersToDataNode(DN dataNode, Set<DataNodeUser> user) throws NotExistException;

    Optional<DN> findByToken(String token);

    DN getBySid(String uuid) throws NotExistException;
}
