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
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private ZipUtil() {

    }

    public static void unzipToDir(Path zipFile, Path destination) throws IOException {

        try (final ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                Path filePath = destination.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Path parent = filePath.getParent();
                    if (Files.notExists(parent)) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(zip, filePath, REPLACE_EXISTING);
                }
                zip.closeEntry();
                entry = zip.getNextEntry();
            }
        }
    }

    public static void addZipEntry(ZipOutputStream zos, String realName, Path file) throws IOException {

        addZipEntry(zos, realName, Files.newInputStream(file));
    }

    public static void addZipEntry(ZipOutputStream zos, String realName, InputStream data) throws IOException {

        byte[] bytes = IOUtils.toByteArray(data);
        ZipEntry entry = new ZipEntry(realName);
        entry.setSize((long) bytes.length);
        zos.putNextEntry(entry);
        zos.write(bytes);
        zos.closeEntry();
    }

}
