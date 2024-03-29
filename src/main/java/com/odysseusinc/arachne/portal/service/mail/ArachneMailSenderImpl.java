/*
 *
 * Copyright 2021 Odysseus Data Services, inc.
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
 * Authors: Alexandr Cumarav
 * Created: April 20, 2021
 *
 */

package com.odysseusinc.arachne.portal.service.mail;

import com.odysseusinc.arachne.portal.model.IUser;
import java.util.Optional;
import net.htmlparser.jericho.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.net.URL;
import java.util.Map;

@Service
public class ArachneMailSenderImpl implements ArachneMailSender {
    private static final Logger LOG = LoggerFactory.getLogger(ArachneMailSenderImpl.class);
    private static final String SIGNATURE = "signature";
    private static final String PATH_TO_TEMPLATES = "/templates/";
    private static final String NAME = "_text";
    private static final String EXTENSION = ".txt";

    @Autowired
    private TemplateEngine templateEngine;

    private JavaMailSender mailSender;

    @Value("${arachne.mail.notifier}")
    private String from;

    @Value("${arachne.mail.signature}")
    private String signature;

    @Value("${arachne.mail.app-title}")
    private String appTitle;

    @Autowired
    public ArachneMailSenderImpl(JavaMailSender mailSender) {

        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void asyncSend(ArachneMailMessage mailMessage) {

        send(mailMessage);
    }

    @Override
    public String buildContent(String templateName, Map<String, Object> parameters) {

        Context context = new Context();
        parameters.put(SIGNATURE, signature);
        context.setVariables(parameters);
        return templateEngine.process(templateName, context);
    }

    @Override
    public void send(ArachneMailMessage mailMessage) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper;
            helper = new MimeMessageHelper(message, true);
            helper.setSubject(mailMessage.getSubject().replaceAll("\\$\\{app-title\\}", appTitle));
            helper.setFrom(from, mailMessage.getFromPersonal().replaceAll("\\$\\{app-title\\}", appTitle));
            helper.setTo(getEmail(mailMessage.getUser()));
            URL templateUrl = this.getClass().getResource(PATH_TO_TEMPLATES + mailMessage.getTemplate() + NAME + EXTENSION);
            String htmlString = buildContent(mailMessage.getTemplate(), mailMessage.getParameters());
            if (templateUrl != null) {
                File textTemplate = new File(templateUrl.getPath());
                if (!textTemplate.isDirectory()) {
                    helper.setText(buildContent(mailMessage.getTemplate() + NAME, mailMessage.getParameters()), htmlString);
                }
            } else {
                Source source = new Source(htmlString);
                String textString = source.getRenderer().toString();
                helper.setText(textString, htmlString);
            }
            mailSender.send(message);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private String getEmail(IUser user) {
        return Optional.ofNullable(user.getContactEmail()).orElseGet(user::getEmail);
    }
}
