package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("agent_traces")
public record AgentTrace(@Id ObjectId id,
    @Field("execution_id") ObjectId executionId,
    @Field("agent_name") String agentName,
    @Field("input") Map<String,Object> input,
    @Field("output") Map<String,Object> output,
    @Field("started_at") Object startedAt,
    @Field("completed_at") Object completedAt,
    @Field("latency_ms") Integer latencyMs,
    @Field("status") String status,
    @Field("model") String model,
    @Field("estimated_tokens") Integer estimatedTokens,
    @Field("estimated_cost_usd") Double estimatedCostUsd,
    @Field("langsmith_run_id") String langsmithRunId,
    @Field("prompt_tokens") Integer promptTokens,
    @Field("completion_tokens") Integer completionTokens
) { }
