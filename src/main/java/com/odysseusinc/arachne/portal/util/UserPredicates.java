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
 * Created: June 08, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.function.Predicate;

public final class UserPredicates {
    public static Predicate<DataNodeUser> dataNodeUserEquals(ArachneUser user) {

        return (u) -> user != null && user.getId().equals(u.getUser().getId());
    }

    public static Predicate<DataNodeUser> dataNodeUserEquals(IUser user) {

        return (u) -> user != null && user.getId().equals(u.getUser().getId());
    }

    public static Predicate<User> userEquals(ArachneUser user) {

        return (u) -> user != null && u.getId().equals(user.getId());
    }
}
