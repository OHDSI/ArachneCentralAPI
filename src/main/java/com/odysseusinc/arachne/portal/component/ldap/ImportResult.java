/*
 *
 * Copyright 2021 Odysseus Data Services, inc.
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
 * Authors: Yaroslav Molodkov, Alexandr Cumarav, Vitaliy Kulakov
 * Created: March 17, 2021
 *
 */

package com.odysseusinc.arachne.portal.component.ldap;

public class ImportResult {

    public enum ImportResultState {
        CREATED,
        UPDATED,
        ERROR
    }

    private String id;
    private ImportResultState state;
    private String message;

    public ImportResult(String id, ImportResultState state) {

        this.id = id;
        this.state = state;
    }

    public ImportResult(String id, ImportResultState state, String message) {

        this.id = id;
        this.state = state;
        this.message = message;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public ImportResultState getState() {

        return state;
    }

    public void setState(ImportResultState state) {

        this.state = state;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }
}
