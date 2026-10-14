package com.dextime.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.Map;

@Service
public class EmailService {

    private final SesClient sesClient;
    private final TemplateEngine templateEngine;
    private final String from;

    public EmailService(SesClient sesClient,
                        TemplateEngine templateEngine,
                        @Value("${aws.email.from}") String from) {
        this.sesClient = sesClient;
        this.templateEngine = templateEngine;
        this.from = from;
    }

    public void sendHtml(String to, String subject, String template, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process(template, context);

        sesClient.sendEmail(SendEmailRequest.builder()
                .source(from)
                .destination(Destination.builder().toAddresses(to).build())
                .message(software.amazon.awssdk.services.ses.model.Message.builder()
                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                        .body(Body.builder()
                                .html(Content.builder().data(html).charset("UTF-8").build())
                                .build())
                        .build())
                .build());
    }
}