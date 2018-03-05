package com.odysseusinc.arachne.portal.service.mail;

import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;

public class NewDataSourceMailMessage<DS extends IDataSource> extends ArachneMailMessage {

    public NewDataSourceMailMessage(String portalUrl, IUser user, DS dataSource) {

        super(user);
        parameters.put("dataSourceName", dataSource.getName());
        parameters.put("dataSourceUrl", portalUrl + "/data-catalog/data-sources/" + dataSource.getId());
    }

    @Override
    protected String getSubject() {

        return "New Data Source registered";
    }

    @Override
    protected String getTemplate() {

        return "mail/new_data_source";
    }
}
