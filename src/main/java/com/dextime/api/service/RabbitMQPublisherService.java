package com.dextime.api.service;

import com.dextime.api.configuration.RabbitMQConfiguration;
import com.dextime.api.dto.RabbitMQMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQPublisherService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishMessage(String entityType, String operation, String id, Object data) {
        RabbitMQMessage message = new RabbitMQMessage(entityType, operation, id, data);
        rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXCHANGE, RabbitMQConfiguration.ROUTING_KEY, message);
    }
}
