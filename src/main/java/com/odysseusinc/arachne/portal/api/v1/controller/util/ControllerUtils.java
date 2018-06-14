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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller.util;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import java.util.List;
import org.springframework.validation.FieldError;

public class ControllerUtils {

    public static JsonResult getFieldErrorsJsonResult(List<FieldError> fieldErrors) {

        JsonResult result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
        for (FieldError fieldError : fieldErrors) {
            result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return result;
    }


    // Sleep from second to five to simulate email sending
    public static void emulateEmailSent() throws InterruptedException {

        Thread.sleep((1L + (long) (Math.random() * 4)) * 1000);
    }

}
