package com.kairos.api.mongo;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.repository.AiDocumentRepository;
import com.kairos.api.mongo.service.MongoHealthService;
import com.kairos.api.mongo.service.ChatPersistenceService;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;

@SpringBootTest(properties={"spring.config.import=","spring.mongodb.uri=mongodb://127.0.0.1:1/isolated_test",
        "spring.mongodb.database=isolated_test","app.mongodb.verify-on-startup=false","app.mongodb.writes-enabled=true",
        "app.security.api-token=example-test-token-with-more-than-32-characters"})
class AiHttpIntegrationTest {
    static final String AUTH="Bearer example-test-token-with-more-than-32-characters";
    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper json;
    @MockitoBean AiDocumentRepository repository;
    @MockitoBean MongoHealthService health;
    @MockitoBean ChatPersistenceService runtime;
    MockMvc http;
    @BeforeEach void setup() {
        http=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        when(repository.find(any(),eq(AiFixtures.REF))).thenReturn(AiFixtures.parent());
        when(repository.insert(any(),any(Document.class))).thenAnswer(call->call.getArgument(1));
        when(repository.list(any(),anyMap(),anyInt(),anyInt())).thenReturn(List.of());
    }
    @ParameterizedTest @EnumSource(AiResource.class)
    void servesCreateReadPatchAndDeleteContractsForEveryCollection(AiResource resource) throws Exception {
        String base="/api/v1/"+resource.route;
        var result=http.perform(post(base).header("Authorization",AUTH).contentType("application/json")
                .content(json.writeValueAsString(AiFixtures.body(resource))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.data").doesNotExist()).andReturn();
        var response=json.readTree(result.getResponse().getContentAsString());String id=response.get("id").asString();
        var inserted=org.mockito.ArgumentCaptor.forClass(Document.class);verify(repository).insert(eq(resource),inserted.capture());
        var document=inserted.getValue();var objectId=document.getObjectId("_id");
        when(repository.find(resource,objectId)).thenReturn(document);
        http.perform(get(base+"/"+id).header("Authorization",AUTH)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        var patch=AiFixtures.patch(resource);
        when(repository.patch(eq(resource),eq(objectId),anyMap())).thenReturn(document);
        http.perform(patch(base+"/"+id).header("Authorization",AUTH).contentType("application/json")
                .content(json.writeValueAsString(patch)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        var updates=org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(repository).patch(eq(resource),eq(objectId),updates.capture());
        patch.forEach((field,value)->{
            String stored=resource.storedField(field);
            if (value instanceof Map<?,?> nested) nested.forEach((key,item)->
                    Assertions.assertEquals(item,updates.getValue().get(stored+"."+key)));
            else Assertions.assertEquals(value,updates.getValue().get(stored));
        });
        when(repository.delete(resource,objectId)).thenReturn(document);
        http.perform(delete(base+"/"+id).header("Authorization",AUTH)).andExpect(status().isNoContent());
    }
    @Test void rejectsPatchesWithoutEffectiveChanges() throws Exception {
        for (var patch:List.of(Map.of(),Map.of("input",Map.of()),Map.of("output",Map.of()))) {
            http.perform(patch("/api/v1/agent-traces/"+AiFixtures.REF.toHexString())
                    .header("Authorization",AUTH).contentType("application/json")
                    .content(json.writeValueAsString(patch)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
        verify(repository,never()).patch(any(),any(),anyMap());
    }
    @Test void protectsBusinessDataWithBearerAndDoesNotLeakToken() throws Exception {
        http.perform(get("/api/v1/conversations")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        http.perform(get("/api/v1/conversations").header("Authorization","Bearer invalid-token")).andExpect(status().isUnauthorized());
        http.perform(get("/api/v1/conversations").header("Authorization",AUTH)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray()).andExpect(jsonPath("$.page").value(0));
    }
    @Test void rejectsMalformedIdentifiersPayloadsAndUnobservedResources() throws Exception {
        http.perform(get("/api/v1/conversations/bad-id").header("Authorization",AUTH)).andExpect(status().isBadRequest());
        http.perform(get("/api/v1/users").header("Authorization",AUTH)).andExpect(status().isNotFound());
        http.perform(post("/api/v1/conversations").header("Authorization",AUTH).contentType("application/json")
                .content("{\"userId\":1,\"status\":\"ACTIVE\",\"unknown\":true}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        verify(repository,never()).insert(any(),any());
    }
    @Test void sanitizesPersistenceFailures() throws Exception {
        when(repository.list(any(),anyMap(),anyInt(),anyInt())).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("private-uri-and-password"));
        http.perform(get("/api/v1/conversations").header("Authorization",AUTH)).andExpect(status().isServiceUnavailable())
                .andExpect(content().json("{\"code\":\"DATABASE_UNAVAILABLE\",\"message\":\"Database operation unavailable.\"}"));
    }
    @Test void runtimeRoutesRequireBearerValidateBodiesAndForwardLease() throws Exception {
        String route="/api/v1/runtime/users/1/conversations/"+AiFixtures.REF.toHexString()+"/turns";
        http.perform(post(route).contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        http.perform(post(route).header("Authorization",AUTH).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        http.perform(post(route).header("Authorization",AUTH).header("X-Chat-Lease","request-1").contentType("application/json")
                .content("{\"turnId\":\"request-1\",\"question\":\"Q\",\"answer\":\"A\"}"))
                .andExpect(status().isOk());
        verify(runtime).appendTurn(eq(1),eq(AiFixtures.REF.toHexString()),
                eq(new com.kairos.api.mongo.dto.request.RuntimeRequests.Turn("request-1","Q","A")),eq("request-1"));
    }
    @Test void acceptsUnknownJudgeFactsAndNewMetricFields() throws Exception {
        var judge=new HashMap<String,Object>(AiFixtures.body(AiResource.JUDGE_EVALUATION));
        judge.put("grounded",null);judge.put("hallucinationDetected",null);
        http.perform(post("/api/v1/judge-evaluations").header("Authorization",AUTH).contentType("application/json")
                .content(json.writeValueAsString(judge))).andExpect(status().isCreated());
        var trace=new HashMap<String,Object>(AiFixtures.body(AiResource.AGENT_TRACE));
        trace.put("langsmithRunId","run-example");trace.put("promptTokens",12);trace.put("completionTokens",5);
        http.perform(post("/api/v1/agent-traces").header("Authorization",AUTH).contentType("application/json")
                .content(json.writeValueAsString(trace))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.promptTokens").value(12)).andExpect(jsonPath("$.langsmithRunId").value("run-example"));
    }
    @Test void runtimeSourcesAcceptOnlyPublicCategories() throws Exception {
        String route="/api/v1/runtime/users/1/conversations/"+AiFixtures.REF.toHexString()+"/turns";
        var body=new HashMap<String,Object>(Map.of("turnId","source-turn","question","Q","answer","A"));
        body.put("sources",List.of("pdf","database","memory"));
        http.perform(post(route).header("Authorization",AUTH).contentType("application/json").content(json.writeValueAsString(body)))
                .andExpect(status().isOk());
        verify(runtime).appendTurn(eq(1),eq(AiFixtures.REF.toHexString()),
                eq(new com.kairos.api.mongo.dto.request.RuntimeRequests.Turn("source-turn","Q","A",List.of("pdf","database","memory"))),isNull());
        body.put("sources",List.of("private_collection"));
        http.perform(post(route).header("Authorization",AUTH).contentType("application/json").content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private_collection"))));
        verify(runtime,times(1)).appendTurn(anyInt(),anyString(),any(),any());
    }
}
