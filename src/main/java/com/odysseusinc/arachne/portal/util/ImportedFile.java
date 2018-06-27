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
 * Created: August 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import org.springframework.core.io.AbstractResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;

// This class is required only due to unavailability to serialize incoming MultipartFile
public class ImportedFile extends AbstractResource implements Serializable {

    private String originalFilename;
    private byte[] data;

    public ImportedFile() {
    }

    public ImportedFile(String originalFilename, byte[] data) {

        this.data = data;
        this.originalFilename = Objects.nonNull(originalFilename) ? originalFilename : "";
    }

    public String getOriginalFilename() {

        return originalFilename;
    }

    @Override
    public String getFilename() {

        return originalFilename;
    }

    @Override
    public String getDescription() {

        return originalFilename;
    }

    @Override
    public boolean exists() {

        return true;
    }

    @Override
    public long contentLength() throws IOException {

        return (long)this.data.length;
    }

    @Override
    public InputStream getInputStream() throws IOException {

        return new ByteArrayInputStream(data);
    }

    public byte[] getData() {

        return data;
    }

    public void setData(byte[] data) {

        this.data = data;
    }
}
