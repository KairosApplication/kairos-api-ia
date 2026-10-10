package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("rag_documents")
public record RagDocument(@Id ObjectId id,
    @Field("title") String title,
    @Field("description") String description,
    @Field("source_type") String sourceType,
    @Field("source_url") String sourceUrl,
    @Field("active") Boolean active,
    @Field("created_at") Object createdAt
) { }
