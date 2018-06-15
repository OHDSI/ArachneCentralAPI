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
 * Authors: Anastasiia Klochkova
 * Created: June 15, 2018
 *
 */

package com.odysseusinc.arachne.portal.model;

import java.util.HashMap;
import java.util.Map;

public class DataSourceFields {
    private static Map<String, String> fields = new HashMap<>();
    public static final String UI_NAME = "name";

    static {
        fields.put("organization", "organization");
        fields.put("modelType", "model_type");
        fields.put("cdmVersion", "cdm_version");
        fields.put("accessType", "access_type");
        fields.put("executionPolicy", "execution_policy");
        fields.put("publishedLabel", "published");
        fields.put(UI_NAME, "dn.name");
    }

    public static Map<String, String> getFields() {

        return fields;
    }
}
