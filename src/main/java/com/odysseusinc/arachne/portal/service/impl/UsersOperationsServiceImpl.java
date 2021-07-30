package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.BatchOperationType;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.repository.BaseRawUserRepository;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.SolrService;
import com.odysseusinc.arachne.portal.service.UsersOperationsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.odysseusinc.arachne.portal.model.PortalConstants.TENANTS_USERS_TABLE_NAME;

@Service
@Transactional
public class UsersOperationsServiceImpl implements UsersOperationsService {

    private final BaseUserService baseUserService;
    private final BaseRawUserRepository baseRawUserRepository;
    private final SolrService solrService;

    public UsersOperationsServiceImpl(BaseUserService baseUserService, BaseRawUserRepository baseRawUserRepository, SolrService solrService) {

        this.baseUserService = baseUserService;
        this.baseRawUserRepository = baseRawUserRepository;
        this.solrService = solrService;
    }

    @Override
    public void deleteAllUsers(List<IUser> users) {

        baseRawUserRepository.deleteInBatch(users);
        users.forEach(solrService::delete);
    }

    @Override
    public void performBatchOperation(final List<String> ids, final BatchOperationType type) {

        final List<IUser> users = baseRawUserRepository.findByIdIn(UserIdUtils.uuidsToIds(ids));

        switch (type) {
            case CONFIRM:
                toggleFlag(users, IUser::getEmailConfirmed, IUser::setEmailConfirmed);
                break;
            case DELETE:
                deleteAllUsers(users);
                break;
            case ENABLE:
                toggleFlag(users, IUser::getEnabled, IUser::setEnabled);
                break;
            case RESEND:
                users.forEach(baseUserService::resendActivationEmail);
                break;
            default:
                throw new IllegalArgumentException("Batch operation type " + type + " isn't supported");
        }
    }

    @Override
    public Set<Long> checkIfUsersAreDeletable(final Set<Long> users) {

        final String delimiter = ",";
        final String userIds = users.stream().map(String::valueOf).collect(Collectors.joining(delimiter));
        final String deletableUsers = baseRawUserRepository.checkIfUsersAreDeletable(userIds, TENANTS_USERS_TABLE_NAME);
        return Stream.of(org.apache.commons.lang3.StringUtils.split(deletableUsers, delimiter)).map(Long::valueOf).collect(Collectors.toSet());
    }

    private void toggleFlag(
            final List<IUser> entities,
            final Function<IUser, Boolean> getter,
            final BiConsumer<IUser, Boolean> setter) {

        for (final IUser entity : entities) {
            setter.accept(entity, !getter.apply(entity));
        }
        baseRawUserRepository.saveAll(entities);
    }
}