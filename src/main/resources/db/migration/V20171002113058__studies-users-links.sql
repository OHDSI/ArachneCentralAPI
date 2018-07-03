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
 * Created: October 02, 2017
 *
 */

WITH main_admin AS (SELECT id
                    FROM public.users
                    WHERE email = 'admin@odysseusinc.com')
INSERT INTO public.studies_users (study_id, user_id, role, status, created_by)
  (SELECT
     id,
     (SELECT id
      FROM main_admin),
     'LEAD_INVESTIGATOR',
     'APPROVED',
     (SELECT id
      FROM main_admin)
   FROM public.studies
   WHERE title LIKE 'Study #%' AND id NOT IN (SELECT study_id
                    FROM public.studies_users
                    WHERE user_id = (SELECT id
                                     FROM main_admin)));