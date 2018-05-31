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
 * Created: September 28, 2017
 *
 */

ALTER TABLE datanodes DROP CONSTRAINT IF EXISTS name_not_virtual_uq;
DROP INDEX IF EXISTS name_not_virtual_uq;

ALTER TABLE data_sources DROP CONSTRAINT IF EXISTS name_uk;
DROP INDEX IF EXISTS name_uk;

CREATE UNIQUE INDEX IF NOT EXISTS datanodes_not_virtual_name_uq
  ON datanodes (name) WHERE (is_virtual = FALSE);

CREATE UNIQUE INDEX IF NOT EXISTS data_sources_name_uk
  ON data_sources (data_node_id, name);