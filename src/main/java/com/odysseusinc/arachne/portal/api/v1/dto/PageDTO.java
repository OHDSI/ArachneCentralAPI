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
 * Created: March 16, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

public class PageDTO extends DTO {

    protected Integer page;
    protected Integer pageSize;

    public PageDTO() {

        this.page = 1;
        this.pageSize = 10;
    }

    public PageDTO(PageDTO other) {

        this.page = other.page;
        this.pageSize = other.pageSize;
    }

    public Integer getPage() {

        return page;
    }

    public Integer getPageablePage() {

        return page == null ? 0 : (page - 1);
    }

    public void setPage(Integer page) {

        this.page = page;
    }

    public Integer getPageSize() {

        return pageSize;
    }

    public void setPageSize(Integer pageSize) {

        this.pageSize = pageSize;
    }

}
