/*
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
 * Authors: Anton Gackovka
 * Created: February 19, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.List;
import java.util.stream.Collectors;

public class DataSourceSolrExtractors {
    public static class TitleExtractor implements SolrFieldExtractor<IDataSource> {
        @Override
        public String extract(final IDataSource ds) {

            return ds.getName();
        }
    }

    public static class TenantsExtractor implements SolrFieldExtractor<IDataSource> {
        @Override
        public List<Long> extract(final IDataSource ds) {

            return ds.getTenants().stream().map(Tenant::getId).collect(Collectors.toList());
        }
    }
    
    public static class DataNodeNameExtractor implements SolrFieldExtractor<IDataSource> {
        @Override
        public String extract(final IDataSource ds) {

            return ds.getDataNode().getName();
        }
    }

    public static class OrganizationNameExtractor implements SolrFieldExtractor<IDataSource> {
        @Override
        public String extract(final IDataSource ds) {

            return ds.getDataNode().getOrganization().getName();
        }
    }
}
