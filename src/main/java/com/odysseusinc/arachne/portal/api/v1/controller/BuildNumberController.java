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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonBuildNumberDTO;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class BuildNumberController {
    private static final String SOURCE = "META-INF/version.properties";
    private final Logger log = LoggerFactory.getLogger(BuildNumberController.class);

    private final String buildNumber;
    private final String buildId;
    private final String projectVersion;

    public BuildNumberController() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(SOURCE);
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            log.warn("Unable to read [{}], version info unavailable. Error details: {}.", SOURCE, e.getMessage());
        }
        this.projectVersion = properties.getProperty("version", "dev");;
        this.buildNumber = properties.getProperty("buildNumber", "dev");
        this.buildId = properties.getProperty("buildId", "dev");;
        log.info("Version [{}], build [{}] @ [{}]", projectVersion, buildNumber, buildId);
    }


    @ApiOperation(value = "Get build number.", hidden = true)
    @RequestMapping(value = "/api/v1/build-number", method = RequestMethod.GET)
    public CommonBuildNumberDTO buildNumber() {
        CommonBuildNumberDTO buildNumberDTO = new CommonBuildNumberDTO();
        buildNumberDTO.setBuildNumber(buildNumber);
        buildNumberDTO.setBuildId(buildId);
        buildNumberDTO.setProjectVersion(projectVersion);
        return buildNumberDTO;
    }

}
