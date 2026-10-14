package com.dextime.api.service;

import com.dextime.api.configuration.RabbitMQConfiguration;
import com.dextime.api.dto.EmailMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailToQueue(String to, String subject, String template, Map<String, Object> variables) {
        try {
            EmailMessageDto message = new EmailMessageDto(to, subject, template, variables);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfiguration.EXCHANGE,
                    RabbitMQConfiguration.ROUTING_KEY,
                    message
            );
            log.info("Email message sent to RabbitMQ queue for recipient: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email message to RabbitMQ queue", e);
        }
    }
}