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

INSERT INTO system_settings (group_id, label, name, type)
VALUES
  ((SELECT id
    FROM system_settings_groups
    WHERE name = 'mail.server'), 'Email address', 'arachne.mail.notifier', 'text'),
  ((SELECT id
    FROM system_settings_groups
    WHERE name = 'mail.server'), 'Smtp authentication', 'spring.mail.properties.mail.smtp.auth', 'checkbox'),
  ((SELECT id
    FROM system_settings_groups
    WHERE name = 'mail.server'), 'Smtp starttls enabled', 'spring.mail.properties.mail.smtp.starttls.enable',
   'checkbox'),
  ((SELECT id
    FROM system_settings_groups
    WHERE name = 'mail.server'), 'Smtp starttls required', 'spring.mail.properties.mail.smtp.starttls.required', 'checkbox')
ON CONFLICT DO NOTHING;