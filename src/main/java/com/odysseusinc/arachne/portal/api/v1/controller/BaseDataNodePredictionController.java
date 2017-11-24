package com.odysseusinc.arachne.portal.api.v1.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonPredictionDTO;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.messaging.BaseDataNodeMessageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.jms.JMSException;
import java.util.List;

public class BaseDataNodePredictionController<DN extends DataNode> extends BaseController {

    private final BaseDataNodeService<DN> dataNodeService;
    private final BaseDataNodeMessageService<DN> messageService;

    @Autowired
    public BaseDataNodePredictionController(BaseDataNodeService<DN> dataNodeService,
                                            BaseDataNodeMessageService<DN> messageService) {

        this.dataNodeService = dataNodeService;
        this.messageService = messageService;
    }

    @RequestMapping(value = "/api/v1/data-nodes/{dataNodeId}/predictions", method = GET)
    @ApiOperation("Returns list of patient level predictions from datanode")
    public List<CommonPredictionDTO> listPredictions(@PathVariable("dataNodeId") Long dataNodeId)
            throws JMSException {

        DN dataNode = dataNodeService.getById(dataNodeId);
        return messageService.getDataList(dataNode, CommonAnalysisType.PREDICTION);
    }
}
