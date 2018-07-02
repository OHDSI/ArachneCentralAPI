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
 * Created: June 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.dto.BreadcrumbDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.service.BreadcrumbService;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UtilController extends BaseController {

    private final GenericConversionService conversionService;
    private final BreadcrumbService breadcrumbService;

    @Autowired
    public UtilController(GenericConversionService conversionService, BreadcrumbService breadcrumbService) {

        this.conversionService = conversionService;
        this.breadcrumbService = breadcrumbService;
    }

    @ApiOperation("Get breadcrumbs for entity.")
    @RequestMapping(value = "/api/v1/utils/breadcrumbs/{entity}/{id}", method = RequestMethod.GET)
    public JsonResult<List<BreadcrumbDTO>> getBreadcrumbs(
            @PathVariable("entity") BreadcrumbType entity,
            @PathVariable("id") Long id
    ) throws NotExistException {

        List<BreadcrumbDTO> breadcrumbDTOList = breadcrumbService.getBreadcrumbs(entity, id)
                .stream()
                .map(crumb -> conversionService.convert(crumb, BreadcrumbDTO.class))
                .collect(Collectors.toList());

        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR, breadcrumbDTOList);
    }
}
