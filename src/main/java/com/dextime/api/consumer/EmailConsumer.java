package com.dextime.api.consumer;

import com.dextime.api.configuration.RabbitMQConfiguration;
import com.dextime.api.dto.EmailMessageDto;
import com.dextime.api.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfiguration.QUEUE)
    public void consumeEmailMessage(EmailMessageDto message) {
        try {
            emailService.sendHtml(
                    message.getTo(),
                    message.getSubject(),
                    message.getTemplate(),
                    message.getVariables()
            );
            log.info("Email sent successfully via RabbitMQ to: {}", message.getTo());
        } catch (Exception e) {
            log.error("Error processing email message from queue for recipient: {}", message.getTo(), e);
        }
    }
}