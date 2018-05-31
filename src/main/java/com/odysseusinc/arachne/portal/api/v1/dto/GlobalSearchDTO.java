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
 * Created: February 15, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.ArrayList;
import java.util.List;

public class GlobalSearchDTO {

    private String id;
    private String label;
    private List<BreadcrumbDTO> breadcrumbs;
    private List<HighlightDTO> highlight = new ArrayList<>();

    public String getId() {

        return id;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(final String label) {

        this.label = label;
    }

    public List<BreadcrumbDTO> getBreadcrumbs() {

        return breadcrumbs;
    }

    public void setBreadcrumbs(final List<BreadcrumbDTO> breadcrumbs) {

        this.breadcrumbs = breadcrumbs;
    }

    public List<HighlightDTO> getHighlight() {

        return highlight;
    }

    public void setHighlight(final List<HighlightDTO> highlight) {

        this.highlight = highlight;
    }
    
    public void addHighlight(final String field, final String value) {
        
        this.getHighlight().add(new HighlightDTO(field, value));
    }

    private class HighlightDTO {

        public HighlightDTO(final String field, final String value) {

            this.field = field;
            this.value = value;
        }

        private String field;
        private String value;

        public String getField() {

            return field;
        }

        public void setField(final String field) {

            this.field = field;
        }

        public String getValue() {

            return value;
        }

        public void setValue(final String value) {

            this.value = value;
        }

        @Override
        public String toString() {

            return "HighlightDTO{" +
                    "field='" + field + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
