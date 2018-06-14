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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonBuildNumberDTO;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class BuildNumberController {

    @Value("${build.number}")
    private String buildNumber;
    @Value("${build.id}")
    private String buildId;
    @Value("${project.version}")
    private String projectVersion;

    @ApiOperation(value = "Get build number.", hidden = true)
    @RequestMapping(value = "/api/v1/build-number", method = RequestMethod.GET)
    public CommonBuildNumberDTO buildNumber(HttpServletRequest request) {

        CommonBuildNumberDTO buildNumberDTO = new CommonBuildNumberDTO();
        buildNumberDTO.setBuildNumber(getBuildNumber());
        buildNumberDTO.setBuildId(getBuildId());
        buildNumberDTO.setProjectVersion(getProjectVersion());
        return buildNumberDTO;
    }

    public String getBuildNumber() {

        return buildNumber;
    }

    public String getProjectVersion() {

        return projectVersion;
    }

    public String getBuildId() {

        return buildId;
    }
}
