/*
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
 * Authors: Anton Gackovka
 * Created: December 13, 2017
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.odysseusinc.arachne.portal.exception.ArachneSystemRuntimeException;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.office.LocalOfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToPdfConverter {

    public static final String DOC_TYPE = "docx";
    private static final Logger log = LoggerFactory.getLogger(ToPdfConverter.class);

    @Autowired(required = false)
    private DocumentConverter converter;

    @Autowired(required = false)
    private LocalOfficeManager officeManager;

    public byte[] convert(final byte[] docFile) {

        if (converter == null) {
            return docFile;
        }

        recreateJodConverterWorkingFolderIfNoLongerExists();

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            converter
                    .convert(new ByteArrayInputStream(docFile))
                    .as(
                            DefaultDocumentFormatRegistry.getFormatByExtension(DOC_TYPE))
                    .to(outputStream)
                    .as(DefaultDocumentFormatRegistry.getFormatByExtension(CommonFileUtils.TYPE_PDF))
                    .execute();
            return outputStream.toByteArray();
        } catch (final OfficeException | IOException e) {
            throw new ArachneSystemRuntimeException("Document conversion failure", e);
        }
    }

    private void recreateJodConverterWorkingFolderIfNoLongerExists() {

        if (officeManager != null) {
            final File tempFile = officeManager.makeTemporaryFile();
            final Path currentTempDir = tempFile.toPath().getParent();
            if (!currentTempDir.toFile().exists()) {
                log.info("Recreating JodConverter temp folder: {}", currentTempDir);
                try {
                    Files.createDirectories(currentTempDir);
                } catch (IOException ex) {
                    throw new ArachneSystemRuntimeException("Cannot restore officeManager temp folder: " + currentTempDir, ex);
                }
            }
        }
    }
}