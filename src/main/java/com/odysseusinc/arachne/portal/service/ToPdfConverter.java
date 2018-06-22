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
 * Created: December 13, 2017
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.jodconverter.DocumentConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToPdfConverter {

    public static final String docType = "docx";

    @Autowired(required = false)
    private DocumentConverter converter;

    public byte[] convert(final byte[] docFile) {

        if (converter == null) {
            return docFile;
        }

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            converter
                    .convert(new ByteArrayInputStream(docFile))
                    .as(
                            DefaultDocumentFormatRegistry.getFormatByExtension(docType))
                    .to(outputStream)
                    .as(DefaultDocumentFormatRegistry.getFormatByExtension(CommonFileUtils.TYPE_PDF))
                    .execute();
            return outputStream.toByteArray();
        } catch (final OfficeException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
