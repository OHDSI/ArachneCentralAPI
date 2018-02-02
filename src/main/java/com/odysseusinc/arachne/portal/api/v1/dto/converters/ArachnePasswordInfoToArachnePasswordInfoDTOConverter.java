package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.ArachnePasswordInfoDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.ComplexRuleInfoDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.RuleInfoDTO;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordInfo;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ComplexRuleInfo;
import com.odysseusinc.arachne.portal.security.passwordvalidator.RuleInfo;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ArachnePasswordInfoToArachnePasswordInfoDTOConverter
        extends BaseConversionServiceAwareConverter<ArachnePasswordInfo, ArachnePasswordInfoDTO> {

    @Override
    public ArachnePasswordInfoDTO convert(ArachnePasswordInfo source) {

        final Set<RuleInfoDTO> ruleInfoDTOS = source.getRules().stream().map(this::convert).collect(Collectors.toSet());
        return new ArachnePasswordInfoDTO(ruleInfoDTOS);
    }

    private RuleInfoDTO convert(RuleInfo ruleInfo) {

        RuleInfoDTO dto;
        final String description = ruleInfo.getDescription();
        if (ruleInfo instanceof ComplexRuleInfo) {
            final Set<RuleInfo> rules = ((ComplexRuleInfo) ruleInfo).getRules();
            final Set<RuleInfoDTO> ruleDTOs = rules.stream().map(this::convert).collect(Collectors.toSet());
            dto = new ComplexRuleInfoDTO(description, ruleDTOs);
        } else {
            dto = new RuleInfoDTO(description);
        }
        return dto;
    }
}
