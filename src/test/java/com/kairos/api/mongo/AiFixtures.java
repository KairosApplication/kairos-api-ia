package com.kairos.api.mongo;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.mapper.AiResponseMapper;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
public final class AiFixtures {
    public static final ObjectId REF=new ObjectId("000000000000000000000001");
    public static Map<String,Object> body(AiResource resource) {
        return switch(resource) {
            case CONVERSATION -> Map.of("userId",1,"status","ACTIVE");
            case FEEDBACK -> Map.of("userId",1,"executionId",REF.toHexString(),"rating",5);
            case USER_MEMORY -> Map.of("userId",1);
            case AGENT_TRACE -> Map.of("executionId",REF.toHexString(),"agentName","example","input",Map.of(),"output",Map.of(),"latencyMs",0,"status","SUCCESS");
            case GUARDRAIL_EVENT -> Map.of("executionId",REF.toHexString(),"guardrailType","INPUT_VALIDATION","action","ALLOW","reason","test");
            case JUDGE_EVALUATION -> Map.of("executionId",REF.toHexString(),"judgeAgent","test","score",0.92,"grounded",true,"hallucinationDetected",false,"decision","APPROVE","justification","test");
            case RAG_CHUNK -> Map.of("documentId",REF.toHexString(),"chunkIndex",0,"content","synthetic","embedding",List.of(0.1,0.2));
            case AI_EXECUTION -> Map.of("userId",1,"conversationId",REF.toHexString(),"input","synthetic","status","IN_PROGRESS");
            case RAG_DOCUMENT -> Map.of("title","synthetic","description","test","sourceType","WEB","sourceUrl","https://example.invalid","active",false);
        };
    }
    public static Map<String,Object> patch(AiResource resource) {
        return switch(resource) {
            case CONVERSATION -> Map.of("status","CLOSED");
            case FEEDBACK -> Map.of("rating",4);
            case USER_MEMORY -> Map.of("userId",2);
            case AGENT_TRACE -> Map.of("input",Map.of("prompt","updated"));
            case GUARDRAIL_EVENT -> Map.of("reason","updated");
            case JUDGE_EVALUATION -> Map.of("justification","updated");
            case RAG_CHUNK -> Map.of("content","updated");
            case AI_EXECUTION -> Map.of("status","COMPLETED");
            case RAG_DOCUMENT -> Map.of("title","updated");
        };
    }
    public static AiResponseMapper mapper() throws Exception {
        var conversions=MongoCustomConversions.create(adapter->{});
        var context=new MongoMappingContext();context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());context.afterPropertiesSet();
        var converter=new MappingMongoConverter(NoOpDbRefResolver.INSTANCE,context);
        converter.setCustomConversions(conversions);converter.afterPropertiesSet();
        return new AiResponseMapper(converter);
    }
    public static Document parent() { return new Document("_id",REF).append("user_id",1); }
}
