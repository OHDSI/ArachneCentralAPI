package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.messaging.BaseDataNodeMessageService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataNodePredictionController extends BaseDataNodePredictionController<DataNode> {

    public DataNodePredictionController(BaseDataNodeService<DataNode> dataNodeService, BaseDataNodeMessageService<DataNode> messageService) {

        super(dataNodeService, messageService);
    }
}
