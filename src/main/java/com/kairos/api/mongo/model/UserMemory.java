package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("user_memory")
public record UserMemory(@Id ObjectId id,
    @Field("user_id") Integer userId,
    @Field("memories") List<MemoryEntry> memories,
    @Field("updated_at") Object updatedAt
) { }
