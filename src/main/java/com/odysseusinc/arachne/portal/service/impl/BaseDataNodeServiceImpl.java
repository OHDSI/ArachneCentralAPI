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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import static org.assertj.core.util.Preconditions.checkNotNull;

import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeJournalEntry;
import com.odysseusinc.arachne.portal.model.DataNodeStatus;
import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.repository.DataNodeJournalRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeStatusRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeUserRepository;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.OrganizationService;
import com.odysseusinc.arachne.portal.util.EntityUtils;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseDataNodeServiceImpl<DN extends DataNode, DS extends IDataSource> implements BaseDataNodeService<DN> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDataNodeServiceImpl.class);
    private static final String IS_NOT_FOUND_EXCEPTION = "Data node with id='%s' is not found";
    private static final String DATANODE_WITH_TOKEN_NOT_EXIST_EXC = "DataNode with suggested token does not exists";
    private static final String USER_ALREADY_LINKED_EXC = "User with id='%s' already linked to DataNode with id='%s'";
    private static final String USER_IS_NOT_LINKED_EXC = "User with id='%s' is not linked to DataNode with id='%s'";
    private static final String LINKING_USER_LOG = "Linking User with id='{}' to DataNode with id='{}'";
    private static final String UNLINKING_USER_LOG = "Unlinking User with id='{}' to DataNode with id='{}'";
    private static final String RELINKING_ALL_USERS_LOG = "Relinking all users for DataNode with id='{}'";

    protected final DataNodeRepository<DN> dataNodeRepository;
    private final DataNodeUserRepository dataNodeUserRepository;
    private final DataNodeStatusRepository dataNodeStatusRepository;
    private final DataNodeJournalRepository dataNodeJournalRepository;
    protected final OrganizationService organizationService;
    protected final BaseDataSourceService<DS> dataSourceService;

    @Autowired
    public BaseDataNodeServiceImpl(DataNodeRepository<DN> dataNodeRepository,
                                   BaseDataSourceService<DS> dataSourceService,
                                   DataNodeUserRepository dataNodeUserRepository,
                                   DataNodeStatusRepository dataNodeStatusRepository,
                                   DataNodeJournalRepository dataNodeJournalRepository,
                                   OrganizationService organizationService) {

        this.dataNodeRepository = dataNodeRepository;
        this.dataNodeUserRepository = dataNodeUserRepository;
        this.dataNodeStatusRepository = dataNodeStatusRepository;
        this.dataNodeJournalRepository = dataNodeJournalRepository;
        this.organizationService = organizationService;
        this.dataSourceService = dataSourceService;
    }


    @Override
    // Does not require permissions because Datanode users can be added after register Datanode only
    public DN create(DN dataNode) {

        checkNotNull(dataNode, "given datanode is null");

        DataNodeStatus dataNodeStatus = dataNodeStatusRepository.findByName("New");
        if (dataNodeStatus == null) {
            throw new IllegalStateException("Unable to found status 'New' for data node");
        }
        dataNode.setStatus(dataNodeStatus);
        dataNode.setToken(UUID.randomUUID().toString().replace("-", ""));
        dataNode.setCreated(new Date());
        DN dataNodeRes = dataNodeRepository.save(dataNode);

        DataNodeJournalEntry journalEntry = new DataNodeJournalEntry();
        journalEntry.setDatanode(dataNodeRes);
        journalEntry.setNewStatus(dataNodeStatus);
        journalEntry.setOldStatus(dataNodeStatus);
        journalEntry.setTimestamp(new Timestamp(System.currentTimeMillis()));

        dataNodeJournalRepository.save(journalEntry);

        return dataNodeRes;
    }

    @Transactional
    @Override
    @PreAuthorize("hasPermission(#dataNode, T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_DATANODE)")
    public DN update(DN dataNode) throws NotExistException, AlreadyExistException, ConstraintViolationException, ValidationException {

        DN existed = dataNodeRepository.findByNameAndVirtualIsFalse(dataNode.getName());
        if (existed != null && !existed.getId().equals(dataNode.getId())) {
            throw new AlreadyExistException("Data node with the same name already exists");
        }
        final DN existsDataNode = getById(dataNode.getId());
        if (dataNode.getName() != null) {
            existsDataNode.setName(dataNode.getName());
        }
        if (dataNode.getDescription() != null) {
            existsDataNode.setDescription(dataNode.getDescription());
        }
        if (dataNode.getOrganization() != null) {
            existsDataNode.setOrganization(organizationService.getOrCreate(dataNode.getOrganization()));
        }
        existsDataNode.setPublished(true);
        DN updated = dataNodeRepository.save(existsDataNode);
        List<DS> dataSources = dataSourceService.getByDataNodeId(updated.getId());
        dataSourceService.indexBySolr(dataSources);
        return updated;
    }

    @Override
    public List<DN> findAllIsNotVirtual() {

        return dataNodeRepository.findAllByVirtualIsFalse();
    }

    @Override
    public List<DN> suggestDataNode(Long userId) {

        return dataNodeRepository.findAllByVirtualIsFalseAndDataNodeUsersUserId(userId);
    }

    @Override
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public DN getBySid(String uuid) throws NotExistException {

        if (uuid != null && !uuid.isEmpty()) {
            final DN dataNode = dataNodeRepository.findBySid(uuid);
            if (dataNode == null) {
                final String message = String.format(IS_NOT_FOUND_EXCEPTION, uuid);
                throw new NotExistException(message, DataNode.class);
            }
            return dataNode;
        } else {
            throw new IllegalArgumentException("unable to find datanode by uuid " + uuid);
        }
    }

    @Override
    @PostAuthorize("principal instanceof T(com.odysseusinc.arachne.portal.model.DataNode) ? true : @ArachnePermissionEvaluator.addPermissions(principal, returnObject)")
    public DN getById(Long id) throws NotExistException {

        if (Objects.nonNull(id)) {
            final DN dataNode = dataNodeRepository.findOne(id);
            if (dataNode == null) {
                final String message = String.format(IS_NOT_FOUND_EXCEPTION, id);
                throw new NotExistException(message, DataNode.class);
            }
            return dataNode;
        } else {
            throw new IllegalArgumentException("unable to find datanode by null id ");
        }
    }

    public DN getByIdUnsecured(Long id) throws NotExistException {

        if (Objects.nonNull(id)) {
            final DN dataNode = dataNodeRepository.findOne(id);
            if (dataNode == null) {
                final String message = String.format(IS_NOT_FOUND_EXCEPTION, id);
                throw new NotExistException(message, DataNode.class);
            }
            return dataNode;
        } else {
            throw new IllegalArgumentException("unable to find datanode by null id ");
        }
    }

    @Transactional
    @Override
    @PreAuthorize("#dataNode == authentication.principal")
    public void linkUserToDataNode(DN dataNode, IUser user)
            throws NotExistException, AlreadyExistException {

        linkUserToDataNodeUnsafe(dataNode, user);
    }

    @Transactional
    @Override
    public void linkUserToDataNodeUnsafe(DN dataNode, IUser user)
            throws NotExistException {

        LOGGER.info(LINKING_USER_LOG, user.getId(), dataNode.getId());
        final DataNodeUser dataNodeUser = new DataNodeUser();
        dataNodeUser.setDataNode(dataNode);
        dataNodeUser.setUser(user);
        saveOrUpdateDataNodeUser(dataNode, dataNodeUser);
    }

    @Transactional
    @Override
    @PreAuthorize("#dataNode == authentication.principal")
    public void unlinkUserToDataNode(DN dataNode, IUser user) throws NotExistException {

        LOGGER.info(UNLINKING_USER_LOG, user.getId(), dataNode.getId());
        final DataNodeUser existDataNodeUser
                = dataNodeUserRepository.findByDataNodeAndUserId(dataNode, user.getId())
                .orElseThrow(() -> {
                    final String message = String.format(USER_IS_NOT_LINKED_EXC, user.getId(),
                            dataNode.getId());
                    return new NotExistException(message, User.class);
                });
        dataNodeUserRepository.delete(existDataNodeUser);
    }

    @Transactional
    @Override
    @PreAuthorize("#dataNode == authentication.principal")
    public void relinkAllUsersToDataNode(DN dataNode, Set<DataNodeUser> dataNodeUsers) throws NotExistException {

        LOGGER.info(RELINKING_ALL_USERS_LOG, dataNode.getId());
        final List<DataNodeUser> existingUsers = dataNodeUserRepository.findByDataNode(dataNode);
        existingUsers.forEach(existingDataNodeUser -> {
            if (!dataNodeUsers.contains(existingDataNodeUser)) {
                dataNodeUserRepository.delete(existingDataNodeUser);
            }
        });
        dataNodeUsers.stream().filter(user -> Objects.nonNull(user.getUser())).forEach(dataNodeUser -> {
            dataNodeUser.setDataNode(dataNode);
            saveOrUpdateDataNodeUser(dataNode, dataNodeUser);
        });
    }

    @Override
    public Optional<DN> findByToken(String token) {

        return dataNodeRepository.findByToken(token, EntityUtils.fromAttributePaths("dataSources"));
    }

    private void saveOrUpdateDataNodeUser(DataNode dataNode, DataNodeUser dataNodeUser) {

        dataNodeUser.setDataNode(dataNode);
        dataNodeUserRepository.findByDataNodeAndUserId(dataNode, dataNodeUser.getUser().getId())
                .ifPresent(existing -> dataNodeUser.setId(existing.getId()));
        dataNodeUserRepository.save(dataNodeUser);
    }
}
