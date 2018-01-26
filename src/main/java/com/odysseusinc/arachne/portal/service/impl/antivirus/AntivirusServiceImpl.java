/*
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: January 22, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl.antivirus;

import com.odysseusinc.arachne.portal.model.AntivirusStatus;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJob;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobAnalysisFileResponseEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobFileType;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobPaperPaperFileResponseEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobPaperProtocolFileResponseEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobResponse;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobResponseEventBase;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobStudyFileResponseEvent;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

@Service
public class AntivirusServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(AntivirusServiceImpl.class);
    private static final String PROCESSING_SCAN_REQUEST = "Processing AntivirusJob with id '{}', fileType '{}'";
    private static final String PROCESSING_SCAN_RESULT = "File with id '{}', fileType '{}' is '{}'";

    private final ApplicationEventPublisher eventPublisher;

    @Value("${antivirus.host}")
    private String antivirusHost;
    @Value("${antivirus.port}")
    private Integer antivirusPort;


    @Autowired
    public AntivirusServiceImpl(ApplicationEventPublisher eventPublisher) {

        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Async(value = "antivirusScanExecutor")
    public void processRequest(AntivirusJobEvent event) throws IOException {

        final AntivirusJob antivirusJob = event.getAntivirusJob();

        final AntivirusJobFileType fileType = antivirusJob.getAntivirusJobFileType();
        final Long fileId = antivirusJob.getFileId();

        logger.debug(PROCESSING_SCAN_REQUEST, fileId, fileType);
        final ScanResult scan = scan(antivirusJob.getContent());

        String description = null;
        AntivirusStatus status;

        if (scan instanceof ScanResult.OK) {
            status = AntivirusStatus.OK;
        } else {
            status = AntivirusStatus.INFECTED;
            description = scan.toString();
        }

        logger.debug(PROCESSING_SCAN_RESULT, fileId, fileType, status);

        publishResponse(fileType, fileId, status, description);
    }

    private ScanResult scan(InputStream inputStream) throws IOException {

        try (InputStream is = inputStream) {
            final ClamavClient clamavClient = new ClamavClient(antivirusHost, antivirusPort);
            return clamavClient.scan(is);
        }
    }

    private void publishResponse(final AntivirusJobFileType fileType, final Long fileId, final AntivirusStatus status, final String description) {

        AntivirusJobResponse antivirusJobResponse = new AntivirusJobResponse(fileId, status, description);
        AntivirusJobResponseEventBase antivirusJobResponseEventBase;
        switch (fileType) {
            case STUDY_FILE:
                antivirusJobResponseEventBase = new AntivirusJobStudyFileResponseEvent(this, antivirusJobResponse);
                break;
            case PAPER_PAPER_FILE:
                antivirusJobResponseEventBase = new AntivirusJobPaperPaperFileResponseEvent(this, antivirusJobResponse);
                break;
            case PAPER_PROTOCOL_FILE:
                antivirusJobResponseEventBase = new AntivirusJobPaperProtocolFileResponseEvent(this, antivirusJobResponse);
                break;
            case ANALYSIS_FILE:
                antivirusJobResponseEventBase = new AntivirusJobAnalysisFileResponseEvent(this, antivirusJobResponse);
                break;
            default:
                throw new IllegalArgumentException();
        }
        eventPublisher.publishEvent(antivirusJobResponseEventBase);
    }
}
