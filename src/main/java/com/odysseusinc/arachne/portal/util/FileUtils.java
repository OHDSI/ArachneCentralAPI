/*
 *  Copyright 2018 Observational Health Data Sciences and Informatics
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Company: Odysseus Data Services, Inc.
 *  Product Owner/Architecture: Gregory Klebanov
 *  Authors: Anton Gackovka
 *  Created: October 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import java.io.InputStream;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.springframework.security.crypto.codec.Base64;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    private FileUtils() {

    }

    public static byte[] getBytes(InputStream inputStream, String contentType) throws IOException {

        byte[] result = IOUtils.toByteArray(inputStream);
        if (checkIfBase64EncodingNeeded(contentType)) {
            result = encode(result);
        }
        return result;
    }

    public static byte[] getBytes(Path path, String contentType) throws IOException {

        byte[] result = Files.readAllBytes(path);
        if (checkIfBase64EncodingNeeded(contentType)) {
            result = encode(result);
        }
        return result;
    }

    public static boolean checkIfBase64EncodingNeeded(String contentType) {

        return Stream.of(CommonFileUtils.TYPE_IMAGE, CommonFileUtils.TYPE_PDF)
                .anyMatch(type -> org.apache.commons.lang3.StringUtils.containsIgnoreCase(contentType, type));
    }

    public static byte[] encode(final byte[] result) {

        return Base64.encode(result);
    }
}
