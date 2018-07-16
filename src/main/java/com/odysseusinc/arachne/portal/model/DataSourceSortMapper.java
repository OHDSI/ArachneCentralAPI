/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataSourceSortMapper {
    
    public static final String NAME = "name";
    
    private static final Map<String, List<String>> fields = new HashMap<>();

    static {
        fields.put("executionPolicy" , singletonList("execution_policy"));
        fields.put("organization"    , singletonList("organization"));
        fields.put("accessType"      , singletonList("access_type"));
        fields.put("cdmVersion"      , singletonList("cdm_version"));
        fields.put("modelType"       , singletonList("model_type"));
        fields.put("publishedLabel"  , singletonList("published"));
        fields.put("dataNode"        , singletonList("dn.name"));
        fields.put(NAME              , singletonList("name"));
    }

    public static List<String> map(final String fieldName) {
        
        return fields.get(fieldName);
    }
    
    public static List<String> map(final Collection<String> fieldNames) {
        
        return fieldNames.stream()
                .map(DataSourceSortMapper::map)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
