/*
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
 * Authors: Anton Gackovka
 * Created: December 13, 2017
 */

package com.odysseusinc.arachne.portal.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.xwpf.converter.core.IXWPFConverter;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

@Component
public class DocToPdfConverter {

    private final PdfOptions options = PdfOptions.create();
    private final IXWPFConverter<PdfOptions> converter = PdfConverter.getInstance();

    void convert(final OutputStream out, final File docFile) throws IOException {

        converter.convert(convertFileToXWPFDocument(docFile), out, options);
    }

    private XWPFDocument convertFileToXWPFDocument(final File file) throws IOException {

        final FileInputStream inputStream = new FileInputStream(file);
        return new XWPFDocument(inputStream);
    }
}
