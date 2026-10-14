package com.dextime.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class SnsService {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public SnsService(SnsClient snsClient,
                      ObjectMapper objectMapper,
                      @Value("${aws.sns.topic.appointment.arn}") String topicArn) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn.trim();
        System.out.println("[TOPIC_ARN=[" + this.topicArn + "]]");
    }

    public void publishAppointmentEvent(String email,
                                        String subject,
                                        String template,
                                        Map<String, String> variables) {
        String message = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "subject", subject,
                "template", template,
                "variables", variables
        ));

        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .subject(subject)
                .message(message)
                .build());
    }
}