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
 * Created: February 15, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.StateProvince;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StateProvinceRepository extends CrudRepository<StateProvince, Long> {

    @Query(nativeQuery = true,
            value = "(select * from states_provinces  "
                    + "where country_id=:country_id and "
                    + "lower(name) like :suggestRequest limit :limit) "
                    + "UNION "
                    + "select * from states_provinces where id=:includeId")
    List<StateProvince> suggest(
            @Param("suggestRequest") String suggestRequest,
            @Param("country_id") Long countryId,
            @Param("limit") Integer limit,
            @Param("includeId") Long includeId);

}
