package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("#{!environment.getProperty('spring.profiles.active').contains('enterprise')}")
public class DataNodeHealthStatusStub {

    public CommonHealthStatus get() {

        return CommonHealthStatus.GREEN;
    }
}
