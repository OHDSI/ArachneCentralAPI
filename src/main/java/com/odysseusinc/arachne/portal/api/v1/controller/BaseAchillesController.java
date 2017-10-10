/**
 *
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: September 08, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAchillesReportDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.dto.AchillesReportDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CharacterizationDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import com.odysseusinc.arachne.portal.model.achilles.AchillesReport;
import com.odysseusinc.arachne.portal.model.achilles.Characterization;
import com.odysseusinc.arachne.portal.repository.BaseDataSourceRepository;
import com.odysseusinc.arachne.portal.repository.AchillesReportRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeRepository;
import com.odysseusinc.arachne.portal.service.AchillesService;
import com.odysseusinc.arachne.portal.util.ConverterUtils;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseAchillesController<DS extends DataSource> {
    protected final AchillesService<DS> achillesService;
    protected final BaseDataSourceRepository<DS> dataSourceRepository;
    protected final GenericConversionService conversionService;
    protected final ObjectMapper objectMapper;
    protected final DataNodeRepository dataNodeRepository;
    protected final AchillesReportRepository achillesReportRepository;
    protected final ConverterUtils converterUtils;

    protected Class<DS> dataSourceClass;

    public BaseAchillesController(BaseDataSourceRepository<DS> dataSourceRepository,
                                  DataNodeRepository dataNodeRepository,
                                  ConverterUtils converterUtils,
                                  AchillesService<DS> achillesService,
                                  ObjectMapper objectMapper,
                                  AchillesReportRepository achillesReportRepository,
                                  GenericConversionService conversionService) {

        this.dataSourceRepository = dataSourceRepository;
        this.dataNodeRepository = dataNodeRepository;
        this.converterUtils = converterUtils;
        this.achillesService = achillesService;
        this.objectMapper = objectMapper;
        this.achillesReportRepository = achillesReportRepository;
        this.conversionService = conversionService;
        dataSourceClass = (Class<DS>) GenericTypeResolver.resolveTypeArgument(this.getClass(), BaseAchillesController.class);
    }

    @ApiOperation("Store Achilles results for given datasource")
    @RequestMapping(value = "datanode/datasource/{uuid}", method = RequestMethod.POST)
    public JsonResult<CharacterizationDTO> receiveStats(
            @PathVariable("uuid") String datasourceUuid,
            @RequestParam(value = "file") MultipartFile data)
            throws NotExistException, IOException {

        DS dataSource = checkDataSource(datasourceUuid);
        Characterization characterization = achillesService.createCharacterization(dataSource, data);
        JsonResult<CharacterizationDTO> result = new JsonResult<>();
        result.setErrorCode(NO_ERROR.getCode());
        result.setResult(conversionService.convert(characterization, CharacterizationDTO.class));
        return result;
    }

    @ApiOperation("List all characterizations for given datasource")
    @RequestMapping(value = "datasource/{uuid}/list", method = RequestMethod.GET)
    public JsonResult<List<CharacterizationDTO>> list(@PathVariable("uuid") String datasourceUuid)
            throws NotExistException {

        DS dataSource = checkDataSource(datasourceUuid);
        List<Characterization> characterizations = achillesService.getCharacterizations(dataSource);
        JsonResult<List<CharacterizationDTO>> result = new JsonResult<>();
        result.setErrorCode(NO_ERROR.getCode());
        List<CharacterizationDTO> dtoList = converterUtils.convertList(characterizations, CharacterizationDTO.class);
        result.setResult(dtoList);
        return result;
    }

    @RequestMapping(value = "datasource/{uuid}/reports", method = RequestMethod.GET)
    public JsonResult<List<AchillesReportDTO>> reports(
            @PathVariable("uuid") String datasourceUuid) throws NotExistException {

        DS dataSource = checkDataSource(datasourceUuid);
        List<AchillesReport> reports = achillesService.getReports(dataSource);
        List<AchillesReportDTO> result = converterUtils.convertList(reports, AchillesReportDTO.class);
        return new JsonResult<>(NO_ERROR, result);
    }

    @ApiOperation("List latest characterization for given datasource")
    @RequestMapping(value = "datasource/{uuid}", method = RequestMethod.GET)
    public JsonResult<CharacterizationDTO> getLatestCharacterization(
            @PathVariable("uuid") String datasourceUuid)
            throws NotExistException {

        DS dataSource = checkDataSource(datasourceUuid);
        Characterization characterization = achillesService.getLatestCharacterization(dataSource)
                .orElseThrow(() ->
                        new NotExistException(String.format("Characterization doesn't exist for dataSource: %s", datasourceUuid),
                                Characterization.class));
        JsonResult<CharacterizationDTO> result = new JsonResult<>();
        result.setErrorCode(NO_ERROR.getCode());
        CharacterizationDTO dto = conversionService.convert(characterization, CharacterizationDTO.class);
        result.setResult(dto);
        return result;
    }

    @ApiOperation("Get file contents")
    @RequestMapping(value = {"datasource/{uuid}/files/{filename:.*}",
            "datasource/{uuid}/files/{filepath:.*}/{filename:.*}"},
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public JsonResult<JsonNode> getFile(@PathVariable("uuid") String datasourceUuid,
                                        @RequestParam(name = "char", required = false) Long characterizationId,
                                        @PathVariable(value = "filepath", required = false) String path,
                                        @PathVariable("filename") String filename) throws NotExistException, IOException {

        final String filepath = StringUtils.isBlank(path) ? filename : path + "/" + filename;
        DS dataSource = checkDataSource(datasourceUuid);
        if (characterizationId == null) {
            characterizationId = achillesService.getLatestCharacterizationId(dataSource);
        }
        AchillesFile file = achillesService.getAchillesFile(characterizationId, filepath)
                .orElseThrow(() -> new NotExistException(String.format("File %s not found",
                        filepath), AchillesFile.class));
        JsonObject jsonObject = file.getData();
        JsonNode node = objectMapper.readTree(new Gson().toJson(jsonObject));
        return new JsonResult<>(NO_ERROR, node);
    }

    @ApiOperation("List Achilles reports")
    @RequestMapping(value = "reports", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<CommonAchillesReportDTO>> listReports() throws NotExistException {

        List<AchillesReport> reports = achillesReportRepository.findAllByOrderBySortOrderAsc();
        List<CommonAchillesReportDTO> result = converterUtils.convertList(reports, CommonAchillesReportDTO.class);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    protected DS checkDataSource(String datasourceUuid) throws NotExistException {

        DS dataSource = dataSourceRepository.findByUuid(datasourceUuid);
        if (dataSource == null) {
            String message = String.format("Datasource with uuid: '%s' not found", datasourceUuid);
            throw new NotExistException(message, dataSourceClass);
        }
        return dataSource;
    }

}
