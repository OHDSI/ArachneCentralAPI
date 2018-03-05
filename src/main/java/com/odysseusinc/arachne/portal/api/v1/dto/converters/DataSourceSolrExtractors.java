/*
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
 * Authors: Anton Gackovka
 * Created: February 19, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataSourceSolrExtractors {
    public static class TitleExtractor implements Function<Object, Object> {
        @Override
        public String apply(final Object o) {

            final DataSource ds = tryConvert(o, "Title");
            return ds.getName();
        }
    }

    public static class TenantsExtractor implements Function<Object, Object> {
        @Override
        public List<Long> apply(final Object o) {

            final DataSource ds = tryConvert(o, "Tenants");
            return ds.getTenants().stream().map(Tenant::getId).collect(Collectors.toList());
        }
    }

    private static DataSource tryConvert(final Object o, final String s) {

        if (!(o instanceof DataSource)) {
            throw new IllegalArgumentException(s + " can be extracted only from DataSource object.");
        }
        return (DataSource) o;
    }
}
