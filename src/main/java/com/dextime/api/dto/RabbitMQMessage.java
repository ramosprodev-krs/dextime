package com.dextime.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RabbitMQMessage {
    private String entityType; // "store", "customer", "provider"
    private String operation; // "create", "update", "delete"
    private String id;
    private Object data; // The DTO object (StoreSnapshotDto, CustomerSnapshotDto, ProviderSnapshotDto)
}
