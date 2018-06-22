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
 * Created: May 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import static com.odysseusinc.arachne.portal.util.UserPredicates.userEquals;

import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtils {
    public static ArachneUser getCurrentUser() {

        ArachneUser user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof ArachneUser) {
            user = (ArachneUser) authentication.getPrincipal();
        }
        return user;
    }

    public static boolean isUserApproved(List<User> approvedUsers, ArachneUser user) {

        Objects.requireNonNull(approvedUsers);
        return Objects.nonNull(user) && approvedUsers.stream().anyMatch(userEquals(user));
    }
}
