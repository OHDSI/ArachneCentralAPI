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
import java.util.Objects;

import java.util.function.Predicate;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

//@Transactional
abstract public class RepositoryBasedStateMachine<O extends HasState<S>, S extends IsState, T extends Transition<O, S>> implements StateMachine<O, S, T> {

    private boolean enabled = Boolean.FALSE;

    private States<S> states;
    private Transitions<T> transitions = new Transitions<>();

    private ObjectRepository<O> objectRepository;
    private StateRepository<S> stateRepository;

    public abstract void reconfigure();

    public RepositoryBasedStateMachine(ObjectRepository<O> objectRepository, StateRepository<S> stateRepository) {
        this.objectRepository = objectRepository;
        this.stateRepository = stateRepository;
    }

    public void loadStates() {

        this.states = new States<>(stateRepository.findAll());
    }

    public S getStateByName(String stateName) {

        return this.states.getStateByName(stateName);
    }

    @Override
    public O moveToState(O object, S state) {

        assertStateMachineIsEnabled();

        if (Objects.equals(object.getState(), state)) {
            return object;
        }

        if (!canTransit(object, state)) {
            throw new IllegalStateMoveException(state.getName());
        }

        object.setState(state);
        return this.objectRepository.save(object);
    }

    @Override
    public O moveToState(O object, String stateName) {

        assertStateMachineIsEnabled();

        return this.moveToState(object, stateRepository.findByName(stateName));
    }

    @Override
    public boolean canTransit(O object, S state) {

        if (object == null || state == null) {
            throw new IllegalArgumentException("Object and State should not be null");
        }

        return this.getTransitionsFrom(object.getState())
                .stream()
                .anyMatch(transition -> Objects.equals(transition.getTo(), state) && !transition.getDiscriminator().test(object));
    }

    @Override
    public boolean createTransition(final T transition) {

        return this.transitions.putTransition(transition);
    }

    @Override
    public boolean isEnabled() {

        return this.enabled;
    }

    @Override
    public List<S> getStates() {

        return this.states.getList();
    }

    @Override
    public List<T> getTransitions() {

        return this.transitions.getList();
    }

    @Override
    public List<T> getTransitionsFrom(final S state) {

        return getTransitionsFrom(state.getName());
    }

    @Override
    public List<T> getTransitionsTo(final S state) {

        return getTransitionsTo(state.getName());
    }

    @Override
    public List<T> getTransitionsFrom(final String stateName) {

        return this.transitions.getListByFromState(stateName);
    }

    @Override
    public List<T> getTransitionsTo(final String stateName) {

        return this.transitions.getListByToState(stateName);
    }

    private List<S> findStatesByNames(List<String> states) {

        return this.states.getStatesByNames(states);
    }

    private void assertStateMachineIsEnabled() {

        if (!this.isEnabled()) {
            throw new StateMachineIsNotEnabled();
        }
    }

    public void verifyStates(List<String> states) {

        List<S> foundStates = findStatesByNames(states);
        if (Objects.equals(states.size(), foundStates.size())) {
            this.enabled = Boolean.TRUE;
        } else {
            throw new StateMachineVerificationException(CollectionUtils.subtract(states, foundStates).toString());
        }
    }
}
