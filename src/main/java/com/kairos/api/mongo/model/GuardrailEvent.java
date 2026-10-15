package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("guardrail_events")
public record GuardrailEvent(@Id ObjectId id,
    @Field("execution_id") ObjectId executionId,
    @Field("guardrail_type") String guardrailType,
    @Field("action") String action,
    @Field("reason") String reason,
    @Field("category") String category,
    @Field("created_at") Object createdAt
) { }
