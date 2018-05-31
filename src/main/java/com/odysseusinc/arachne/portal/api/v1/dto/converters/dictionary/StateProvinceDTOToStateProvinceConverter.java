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
 * Created: February 15, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.dictionary;

import com.odysseusinc.arachne.portal.api.v1.dto.StateProvinceDTO;
import com.odysseusinc.arachne.portal.model.StateProvince;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class StateProvinceDTOToStateProvinceConverter
        extends BaseConversionServiceAwareConverter<StateProvinceDTO, StateProvince> {


    @Override
    public StateProvince convert(StateProvinceDTO dto) {

        StateProvince stateProvince = new StateProvince();
        stateProvince.setId(dto.getId());
        stateProvince.setName(dto.getName());
        stateProvince.setIsoCode(dto.getIsoCode());
        return stateProvince;
    }
}
