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
 * Created: August 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.statemachine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class States<T extends IsState> {

    private List<T> stateList;
    private Map<String, T> stateMap;

    public States(List<T> list) {

        this.stateList = list;
        this.stateMap = new HashMap<>(list.size());
        list.forEach(state -> stateMap.put(state.getName(), state));
    }

    public List<T> getList() {

        return this.stateList;
    }

    public List<T> getStatesByNames(List<String> names) {

        return names.stream().map(this.stateMap::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public T getStateByName(String name) {

        return this.stateMap.get(name);
    }
}
