package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.api.v1.dto.BatchOperationType;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.RawUser;
import com.odysseusinc.arachne.portal.repository.BaseRawUserRepository;
import com.odysseusinc.arachne.portal.service.impl.UsersOperationsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.odysseusinc.arachne.portal.model.PortalConstants.TENANTS_USERS_TABLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsersOperationsServiceTest {

    @Mock
    private BaseUserService baseUserService;
    @Mock
    private BaseRawUserRepository baseRawUserRepository;
    @Mock
    private SolrService solrService;
    private IUser userA;
    private IUser userB;
    private List<String> usersIdsStub;

    @InjectMocks
    private UsersOperationsServiceImpl usersOperationsService;

    @Before
    public void setUp() throws Exception {

        userA = new RawUser();
        userA.setId(1L);
        userB = new RawUser();
        userB.setId(2L);
        usersIdsStub = Collections.emptyList();
    }

    @Test
    public void shouldDeleteUsersRecord() {

        final List<IUser> users = Arrays.asList(userA, userB);
        when(baseRawUserRepository.findByIdIn(any())).thenReturn(users);
        when(baseRawUserRepository.checkIfUsersAreDeletable("1,2", TENANTS_USERS_TABLE_NAME)).thenReturn("1,2");

        usersOperationsService.deleteAllUsers(users);

        verify(baseRawUserRepository).deleteInBatch(users);
        verify(solrService, times(2)).delete(any(IUser.class));
    }

    @Test
    public void shouldToggleUsersEnableFlag() {

        userA.setEnabled(Boolean.TRUE);
        userB.setEnabled(Boolean.FALSE);
        when(baseRawUserRepository.findByIdIn(any())).thenReturn(Arrays.asList(userA, userB));

        usersOperationsService.performBatchOperation(usersIdsStub, BatchOperationType.ENABLE);

        assertThat(userA.getEnabled()).isFalse();
        assertThat(userB.getEnabled()).isTrue();
    }

    @Test
    public void shouldToggleUsersEmailConfirmedFlag() {

        userA.setEmailConfirmed(Boolean.FALSE);
        userB.setEmailConfirmed(Boolean.TRUE);
        when(baseRawUserRepository.findByIdIn(any())).thenReturn(Arrays.asList(userA, userB));

        usersOperationsService.performBatchOperation(usersIdsStub, BatchOperationType.CONFIRM);

        assertThat(userA.getEmailConfirmed()).isTrue();
        assertThat(userB.getEmailConfirmed()).isFalse();
    }

    @Test
    public void shouldResendActivationEmailFlag() {

        when(baseRawUserRepository.findByIdIn(any())).thenReturn(Arrays.asList(userA, userB));

        usersOperationsService.performBatchOperation(usersIdsStub, BatchOperationType.RESEND);

        verify(baseUserService, times(2)).resendActivationEmail(any(IUser.class));
    }

    @Test
    public void shouldParseAndReturnListOfDeletableUsers() {

        when(baseRawUserRepository.checkIfUsersAreDeletable("1,3,7", TENANTS_USERS_TABLE_NAME)).thenReturn("1,7");

        final Set<Long> deletableUserIds = usersOperationsService.checkIfUsersAreDeletable(Sets.newSet(1L, 3L, 7L));

        assertThat(deletableUserIds).containsExactly(1L, 7L);
    }
}