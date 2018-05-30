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
 * Created: December 19, 2017
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.FileDTO;
import com.odysseusinc.arachne.portal.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class FileDtoContentHandler {

    public static FileDtoContentHandler getInstance(final FileDTO fileDto, final File file) {
        return new FileDtoContentHandler(fileDto, file);
    }

    public static FileDtoContentHandler getInstance(final FileDTO fileDto, final byte[] content) {
        return new FileDtoContentHandler(fileDto, content);
    }

    private byte[] content;
    private final FileDTO fileDto;

    private Function<byte[], byte[]> pdfConverter;

    private final Predicate<FileDTO> encodingNeeded = dto -> Stream.of(CommonFileUtils.TYPE_IMAGE, CommonFileUtils.TYPE_PDF)
            .anyMatch(type -> StringUtils.containsIgnoreCase(dto.getDocType(), type));

    private final Predicate<FileDTO> convertToPdfNeeded = dto -> CommonFileUtils.isFileConvertableToPdf(dto.getDocType());

    public FileDtoContentHandler(final FileDTO fileDto, final File file) {

        this.fileDto = fileDto;
        try {
            this.content = Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public FileDtoContentHandler(final FileDTO fileDto, final byte[] content) {

        this.fileDto = fileDto;
        this.content = content;
    }

    public FileDtoContentHandler withPdfConverter(final Function<byte[], byte[]> pdfConverter) {

        this.pdfConverter = pdfConverter;
        return this;
    }

    public FileDTO handle() throws IOException {

        if (convertToPdfNeeded.test(this.fileDto)) {
            if (this.pdfConverter == null) {
                throw new IllegalArgumentException("Pdf converter should be set");
            }
            content = this.pdfConverter.apply(content);
            this.fileDto.setDocType(CommonFileUtils.TYPE_PDF);
        }
        if (encodingNeeded.test(this.fileDto)) {
            content = FileUtils.encode(content);
        }
        this.fileDto.setContent(new String(content));
        return this.fileDto;
    }
}