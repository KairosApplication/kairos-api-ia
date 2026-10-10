package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("feedback")
public record Feedback(@Id ObjectId id,
    @Field("user_id") Integer userId,
    @Field("execution_id") ObjectId executionId,
    @Field("rating") Integer rating,
    @Field("comment") String comment,
    @Field("created_at") Object createdAt
) { }
