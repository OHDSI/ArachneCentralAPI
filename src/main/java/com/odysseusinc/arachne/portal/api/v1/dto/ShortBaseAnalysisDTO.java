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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva
 * Created: January 16, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.commons.api.v1.dto.OptionDTO;
import java.util.Date;

public class ShortBaseAnalysisDTO extends DTO {

    private Long id;
    private Date created;
    private OptionDTO type;

    public ShortBaseAnalysisDTO() {

    }

    public ShortBaseAnalysisDTO(ShortBaseAnalysisDTO other) {

        this.id = other.id;
        this.created = other.created;
        this.type = other.type;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public OptionDTO getType() {

        return type;
    }

    public void setType(OptionDTO type) {

        this.type = type;
    }
}
