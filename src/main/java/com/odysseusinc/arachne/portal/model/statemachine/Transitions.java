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
 * Created: August 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.statemachine;

import com.google.common.collect.ArrayListMultimap;

import java.util.ArrayList;
import java.util.List;


public class Transitions<S extends Transition<? extends HasState<?>, ? extends IsState>> {

    private List<S> transitionList = new ArrayList<>();
    private ArrayListMultimap<String, S> transitionToMap = ArrayListMultimap.create();
    private ArrayListMultimap<String, S> transitionFromMap = ArrayListMultimap.create();

    public Transitions() {}

    public Transitions(List<S> transitions) {
        for (S transition : transitions) {
            putTransition(transition);
        }
    }

    public boolean putTransition(S transition) {

        this.transitionList.add(transition);
        this.transitionToMap.put(transition.getTo().getName(), transition);
        this.transitionFromMap.put(transition.getFrom().getName(), transition);

        return Boolean.TRUE;
    }

    public List<S> getList() {

        return this.transitionList;
    }

    public List<S> getListByToState(String toState) {

        return transitionToMap.get(toState);
    }

    public List<S> getListByFromState(String fromState) {

        return transitionFromMap.get(fromState);
    }

}
