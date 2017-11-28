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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: December 06, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "v_result_files_with_folders")
public class ResultEntity extends AbstractResultFile {

    public ResultEntity() {

    }

    public ResultEntity(String uuid, String label, String realName, String searchPath, String contentType, Date created, Date updated, Submission submission, Boolean manuallyUploaded) {

        super(uuid, label, getCurrentLevelName(realName, searchPath), contentType, created, updated, submission, manuallyUploaded);
    }

    private static String getCurrentLevelName(String realName, String searchPath) {

        return realName.substring(searchPath.equals("/") ? 0 : searchPath.length()).split("/")[0];
    }
}
