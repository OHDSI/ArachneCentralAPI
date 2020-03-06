package com.odysseusinc.arachne.portal.api.v1.controller;


import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.BatchOperationDTO;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.search.UserSearch;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.UsersOperationsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;

@Api(hidden = true)
@RestController
@Secured("ROLE_ADMIN")
public class UsersOperationsAdminController {

    private final BaseUserService baseUserService;
    private final UsersOperationsService usersOperationsService;

    public UsersOperationsAdminController(BaseUserService baseUserService, UsersOperationsService usersOperationsService) {

        this.baseUserService = baseUserService;
        this.usersOperationsService = usersOperationsService;
    }

    @ApiOperation(value = "Run standard operation for the set of users accounts", hidden = true)
    @PostMapping(value = "/api/v1/admin/users/batch")
    public JsonResult doBatchOperation(@RequestBody BatchOperationDTO dto) {

        usersOperationsService.performBatchOperation(dto.getIds(), dto.getType());
        return new JsonResult<>(NO_ERROR);
    }

    @ApiOperation(value = "Get undeletable user ids.", hidden = true)
    @GetMapping(value = "/api/v1/admin/users/ids/undeletable")
    public List<String> getListOfUndeletableUserIdsByFilter(final UserSearch userSearch) {

        final List<IUser> users = baseUserService.getList(userSearch);
        final Set<Long> deletableIds = usersOperationsService.checkIfUsersAreDeletable(users.stream().map(IUser::getId).collect(Collectors.toSet()));
        return users.stream().map(IUser::getId).filter(id -> !deletableIds.contains(id)).map(UserIdUtils::idToUuid).collect(Collectors.toList());
    }
}
