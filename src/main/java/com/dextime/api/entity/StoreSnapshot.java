package com.dextime.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "store_snapshots")
public class StoreSnapshot {
    @Id
    private String id;

    @Indexed
    @Field("provider_id")
    private String providerId;

    @Indexed
    @Field("name")
    private String name;

    @Field("address")
    private String address;
}
