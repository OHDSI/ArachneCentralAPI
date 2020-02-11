package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.api.v1.dto.BatchOperationType;
import com.odysseusinc.arachne.portal.model.IUser;

import java.util.List;
import java.util.Set;

public interface UsersOperationsService {

    Set<Long> checkIfUsersAreDeletable(Set<Long> users);

    void deleteAllUsers(List<IUser> users);

    void performBatchOperation(List<String> ids, BatchOperationType type);
}
