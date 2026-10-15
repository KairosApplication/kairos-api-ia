package com.kairos.api.mongo.model;
import com.kairos.api.mongo.dto.request.*;
import com.kairos.api.mongo.exception.ApiException;
import java.util.Arrays;
import org.springframework.data.mongodb.core.mapping.Field;
public enum AiResource {
    CONVERSATION("conversations","conversations",Conversation.class,ConversationRequest.class),
    FEEDBACK("feedback","feedback",Feedback.class,FeedbackRequest.class),
    USER_MEMORY("user-memories","user_memory",UserMemory.class,UserMemoryRequest.class),
    AGENT_TRACE("agent-traces","agent_traces",AgentTrace.class,AgentTraceRequest.class),
    GUARDRAIL_EVENT("guardrail-events","guardrail_events",GuardrailEvent.class,GuardrailEventRequest.class),
    JUDGE_EVALUATION("judge-evaluations","judge_evaluations",JudgeEvaluation.class,JudgeEvaluationRequest.class),
    RAG_CHUNK("rag-chunks","rag_chunks",RagChunk.class,RagChunkRequest.class),
    AI_EXECUTION("ai-executions","ai_executions",AiExecution.class,AiExecutionRequest.class),
    RAG_DOCUMENT("rag-documents","rag_documents",RagDocument.class,RagDocumentRequest.class);
    public final String route;
    public final String collection;
    public final Class<?> modelType;
    public final Class<?> requestType;
    AiResource(String route, String collection, Class<?> modelType, Class<?> requestType) {
        this.route=route; this.collection=collection; this.modelType=modelType; this.requestType=requestType;
    }
    public static AiResource fromRoute(String route) {
        return Arrays.stream(values()).filter(item->item.route.equals(route)).findFirst()
                .orElseThrow(()->ApiException.notFound("Unknown resource."));
    }
    public String storedField(String name) {
        return Arrays.stream(modelType.getRecordComponents()).filter(field->field.getName().equals(name)).map(field->{
            try {
                Field annotation=modelType.getDeclaredField(field.getName()).getAnnotation(Field.class);
                return annotation==null ? field.getName() : annotation.value();
            } catch (NoSuchFieldException failure) { throw new IllegalStateException("Invalid model mapping."); }
        }).findFirst().orElseThrow(()->ApiException.badRequest("Unknown field."));
    }
    public boolean hasField(String field) {
        return Arrays.stream(requestType.getRecordComponents()).anyMatch(item->item.getName().equals(field));
    }
}
