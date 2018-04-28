package com.odysseusinc.arachne.portal.service.mail;

import com.odysseusinc.arachne.portal.model.IUser;

public class NewUserMailMessage extends ArachneMailMessage {

    public NewUserMailMessage(String portalUrl, IUser user, IUser newUser) {

        super(user);
        parameters.put("userName", newUser.getFullName());
        parameters.put("userUrl", portalUrl + "/expert-finder/profile/" + newUser.getUuid());
    }

    @Override
    protected String getSubject() {

        return "New User registered";
    }

    @Override
    protected String getTemplate() {

        return "mail/new_user";
    }
}
