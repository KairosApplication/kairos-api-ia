package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
public record MemoryEntry(@Field("memory_id") ObjectId memoryId, String type, String content, Double importance,
        @Field("created_at") Object createdAt, @Field("updated_at") Object updatedAt) { }
