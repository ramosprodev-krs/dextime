package com.dextime.api.listener;

import com.dextime.api.configuration.RabbitMQConfiguration;
import com.dextime.api.dto.CustomerSnapshotDto;
import com.dextime.api.dto.ProviderSnapshotDto;
import com.dextime.api.dto.RabbitMQMessage;
import com.dextime.api.dto.StoreSnapshotDto;
import com.dextime.api.service.CustomerSnapshotService;
import com.dextime.api.service.ProviderSnapshotService;
import com.dextime.api.service.StoreSnapshotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQMessageListener.class);

    private final StoreSnapshotService storeSnapshotService;
    private final CustomerSnapshotService customerSnapshotService;
    private final ProviderSnapshotService providerSnapshotService;
    private final ObjectMapper objectMapper;

    public RabbitMQMessageListener(StoreSnapshotService storeSnapshotService,
                                   CustomerSnapshotService customerSnapshotService,
                                   ProviderSnapshotService providerSnapshotService,
                                   ObjectMapper objectMapper) {
        this.storeSnapshotService = storeSnapshotService;
        this.customerSnapshotService = customerSnapshotService;
        this.providerSnapshotService = providerSnapshotService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfiguration.QUEUE)
    public void handleMessage(RabbitMQMessage message) {
        logger.info("Received message: entityType={}, operation={}, id={}", 
                    message.getEntityType(), message.getOperation(), message.getId());

        try {
            switch (message.getEntityType().toLowerCase()) {
                case "store":
                    handleStoreMessage(message);
                    break;
                case "customer":
                    handleCustomerMessage(message);
                    break;
                case "provider":
                    handleProviderMessage(message);
                    break;
                default:
                    logger.warn("Unknown entity type: {}", message.getEntityType());
            }
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
        }
    }

    private void handleStoreMessage(RabbitMQMessage message) {
        StoreSnapshotDto dto = objectMapper.convertValue(message.getData(), StoreSnapshotDto.class);
        
        switch (message.getOperation().toLowerCase()) {
            case "create":
                storeSnapshotService.createStoreSnapshot(dto, dto.getProviderId());
                logger.info("Created store with id: {}", dto.getId());
                break;
            case "update":
                storeSnapshotService.updateStoreSnapshotById(message.getId(), dto);
                logger.info("Updated store with id: {}", message.getId());
                break;
            case "delete":
                storeSnapshotService.deleteStoreSnapshotById(message.getId());
                logger.info("Deleted store with id: {}", message.getId());
                break;
            default:
                logger.warn("Unknown operation for store: {}", message.getOperation());
        }
    }

    private void handleCustomerMessage(RabbitMQMessage message) {
        CustomerSnapshotDto dto = objectMapper.convertValue(message.getData(), CustomerSnapshotDto.class);
        
        switch (message.getOperation().toLowerCase()) {
            case "create":
                customerSnapshotService.createCustomerSnapshot(dto);
                logger.info("Created customer with id: {}", dto.getId());
                break;
            case "update":
                customerSnapshotService.updateCustomerSnapshotById(message.getId(), dto);
                logger.info("Updated customer with id: {}", message.getId());
                break;
            case "delete":
                customerSnapshotService.deleteCustomerSnapshotById(message.getId());
                logger.info("Deleted customer with id: {}", message.getId());
                break;
            default:
                logger.warn("Unknown operation for customer: {}", message.getOperation());
        }
    }

    private void handleProviderMessage(RabbitMQMessage message) {
        ProviderSnapshotDto dto = objectMapper.convertValue(message.getData(), ProviderSnapshotDto.class);
        
        switch (message.getOperation().toLowerCase()) {
            case "create":
                providerSnapshotService.createProviderSnapshot(dto);
                logger.info("Created provider with id: {}", dto.getId());
                break;
            case "update":
                providerSnapshotService.updateProviderSnapshotById(message.getId(), dto);
                logger.info("Updated provider with id: {}", message.getId());
                break;
            case "delete":
                providerSnapshotService.deleteProviderSnapshotById(message.getId());
                logger.info("Deleted provider with id: {}", message.getId());
                break;
            default:
                logger.warn("Unknown operation for provider: {}", message.getOperation());
        }
    }
}
