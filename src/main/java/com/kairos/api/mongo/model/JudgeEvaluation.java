package com.kairos.api.mongo.model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;
@Document("judge_evaluations")
public record JudgeEvaluation(@Id ObjectId id,
    @Field("execution_id") ObjectId executionId,
    @Field("judge_agent") String judgeAgent,
    @Field("score") Double score,
    @Field("grounded") Boolean grounded,
    @Field("hallucination_detected") Boolean hallucinationDetected,
    @Field("decision") String decision,
    @Field("justification") String justification,
    @Field("created_at") Object createdAt
) { }
