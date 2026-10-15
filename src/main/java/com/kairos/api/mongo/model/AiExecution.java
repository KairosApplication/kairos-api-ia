package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("ai_executions")
public record AiExecution(@Id ObjectId id,
    @Field("user_id") Integer userId,
    @Field("conversation_id") ObjectId conversationId,
    @Field("input") String input,
    @Field("status") String status,
    @Field("agents_used") List<String> agentsUsed,
    @Field("started_at") Object startedAt,
    @Field("completed_at") Object completedAt,
    @Field("final_response") String finalResponse,
    @Field("total_latency_ms") Integer totalLatencyMs,
    @Field("estimated_cost_usd") Double estimatedCostUsd
) { }
