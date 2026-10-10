package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("conversations")
public record Conversation(@Id ObjectId id,
    @Field("user_id") Integer userId,
    @Field("status") String status,
    @Field("messages") List<ConversationMessage> messages,
    @Field("summary") String summary,
    @Field("summarized_count") Integer summarizedCount,
    @Field("created_at") Object createdAt,
    @Field("updated_at") Object updatedAt,
    @Field("summary_updated_at") Object summaryUpdatedAt
) { }
