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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base contract of the state machine.
 *
 * @param <O> object class
 * @param <S> state class
 * @param <T> transition class
 */
public interface StateMachine<O extends HasState<S>, S extends IsState, T extends Transition<O, S>> {

    /**
     * Checks whether state machine is enabled, due to the unavailability
     * of using functions of the state machine if it isn't enabled.
     *
     * @return true if SM is enabled
     */
    boolean isEnabled();

    boolean createTransition(T transition);

    List<? extends T> getTransitions();

    List<? extends T> getTransitionsFrom(S state);
    List<? extends T> getTransitionsTo(S state);

    List<? extends T> getTransitionsFrom(String stateName);
    List<? extends T> getTransitionsTo(String stateName);

    List<? extends S> getStates();

    boolean canTransit(O object, S state);

    O moveToState(O object, S state) throws IllegalStateMoveException;
    O moveToState(O object, String stateName) throws IllegalStateMoveException;

    default List<T> getAvailableStates(final O object) {

        return getTransitionsFrom(object.getState()).stream().map(transition -> {
            T copy = transition.getCopy();
            if (transition.getDiscriminator().test(object)) {
                copy.setInfo();
            }
            return copy;
        }).collect(Collectors.toList());
    }
}
