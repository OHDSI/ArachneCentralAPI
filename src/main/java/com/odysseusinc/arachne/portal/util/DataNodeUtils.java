/**
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
 * Created: May 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeRole;
import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.Set;
import java.util.stream.Collectors;

public class DataNodeUtils {

    private DataNodeUtils() {

    }

    public static Set<User> getDataNodeOwners(DataNode dataNode) {

        return dataNode.getDataNodeUsers()
                .stream()
                .filter(u -> u.getDataNodeRole().contains(DataNodeRole.ADMIN))
                .map(DataNodeUser::getUser)
                .collect(Collectors.toSet());
    }

    public static boolean isDataNodeOwner(DataNode datanode, User user) {

        return datanode.getDataNodeUsers().stream()
                .filter(dataNodeUser -> dataNodeUser.getDataNodeRole().contains(DataNodeRole.ADMIN))
                .anyMatch(UserPredicates.dataNodeUserEquals(user));
    }

    public static boolean isDataNodeOwner(DataNode dataNode, Long userId) {

        final User user = new User();
        user.setId(userId);
        return isDataNodeOwner(dataNode, user);
    }

    public static boolean isDataNodeUser(DataNode dataNode, User user) {

        return dataNode.getDataNodeUsers().stream()
                .anyMatch(UserPredicates.dataNodeUserEquals(user));
    }

    public static boolean isDataNodeUser(DataNode dataNode, ArachneUser currentUser) {

        return currentUser != null
                && dataNode.getDataNodeUsers()
                .stream()
                .anyMatch(UserPredicates.dataNodeUserEquals(currentUser));
    }

}
