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
 * Created: November 07, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.SQLDelete;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("'DATA_SOURCE_ENTITY'")
@Table(name = "data_sources_data")
@SQLDelete(sql = "UPDATE data_sources_data "
        + "SET deleted = current_timestamp, health_status = 'NOT_CONNECTED', health_status_description = 'Deleted'"
        + " WHERE id = ?")
public class RawDataSource extends BaseDataSource implements IDataSource, Serializable, HasArachnePermissions {
}
