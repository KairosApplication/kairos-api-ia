package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("rag_chunks")
public record RagChunk(@Id ObjectId id,
    @Field("document_id") ObjectId documentId,
    @Field("chunk_index") Integer chunkIndex,
    @Field("content") String content,
    @Field("embedding") List<Double> embedding
) { }
