package com.kairos.api.mongo.service;
import com.kairos.api.mongo.AiFixtures;
import com.kairos.api.mongo.exception.ApiException;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.repository.AiDocumentRepository;
import jakarta.validation.Validation;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AiResourceServiceTest {
    AiDocumentRepository repository; AiResourceService service;
    @BeforeEach void setup() throws Exception {
        repository=mock(AiDocumentRepository.class);
        service=new AiResourceService(repository,new MutationValidator(Validation.buildDefaultValidatorFactory().getValidator()),AiFixtures.mapper(),true);
        when(repository.find(any(),eq(AiFixtures.REF))).thenReturn(AiFixtures.parent());
        when(repository.insert(any(),any(Document.class))).thenAnswer(call->call.getArgument(1));
    }
    @ParameterizedTest @EnumSource(AiResource.class)
    void createsMapsAndDeletesAllConfirmedResourceTypes(AiResource resource) {
        var created=service.create(resource,AiFixtures.body(resource));
        assertTrue(ObjectId.isValid(created.id()));assertFalse(created.data().containsKey("_id"));
        var captor=org.mockito.ArgumentCaptor.forClass(Document.class);verify(repository).insert(eq(resource),captor.capture());
        var saved=captor.getValue();ObjectId objectId=saved.getObjectId("_id");
        when(repository.find(resource,objectId)).thenReturn(saved);
        assertEquals(created,service.find(resource,created.id()));
        when(repository.delete(resource,objectId)).thenReturn(saved);
        service.delete(resource,created.id());verify(repository).delete(resource,objectId);
    }
    @Test void patchOnlyChangesSubmittedFieldsAndAllowsExplicitOptionalNull() {
        ObjectId id=new ObjectId();Document original=new Document("_id",id).append("user_id",1).append("status","ACTIVE").append("private_future_field","keep");
        when(repository.find(AiResource.CONVERSATION,id)).thenReturn(original);
        when(repository.patch(eq(AiResource.CONVERSATION),eq(id),anyMap())).thenAnswer(call->{var result=new Document(original);result.putAll(call.<Map<String,Object>>getArgument(2));return result;});
        Map<String,Object> changes=new HashMap<>();changes.put("summary",null);
        var response=service.patch(AiResource.CONVERSATION,id.toHexString(),changes);
        var captor=org.mockito.ArgumentCaptor.forClass(Map.class);verify(repository).patch(eq(AiResource.CONVERSATION),eq(id),captor.capture());
        assertEquals(Set.of("summary"),captor.getValue().keySet());assertNull(response.data().get("summary"));assertEquals("ACTIVE",response.data().get("status"));
        assertFalse(response.data().containsKey("private_future_field"));
    }
    @Test void nestedPatchPreservesUnsentMapFields() {
        ObjectId id=new ObjectId();var original=new Document("_id",id).append("execution_id",AiFixtures.REF)
                .append("input",new Document("question","old").append("context","keep"));
        when(repository.find(AiResource.AGENT_TRACE,id)).thenReturn(original);
        when(repository.patch(eq(AiResource.AGENT_TRACE),eq(id),anyMap())).thenReturn(original);
        service.patch(AiResource.AGENT_TRACE,id.toHexString(),Map.of("input",Map.of("question","new")));
        verify(repository).patch(AiResource.AGENT_TRACE,id,Map.of("input.question","new"));
        verify(repository,never()).find(eq(AiResource.AI_EXECUTION),any());
    }
    @Test void checksMissingReferenceBeforeInsert() {
        when(repository.find(AiResource.AI_EXECUTION,AiFixtures.REF)).thenReturn(null);
        assertEquals(404,assertThrows(ApiException.class,()->service.create(AiResource.FEEDBACK,AiFixtures.body(AiResource.FEEDBACK))).status.value());
        verify(repository,never()).insert(any(),any());
    }
    @Test void rejectsUserMismatchWithoutPersisting() {
        when(repository.find(AiResource.CONVERSATION,AiFixtures.REF)).thenReturn(new Document("_id",AiFixtures.REF).append("user_id",2));
        assertEquals(409,assertThrows(ApiException.class,()->service.create(AiResource.AI_EXECUTION,AiFixtures.body(AiResource.AI_EXECUTION))).status.value());
        verify(repository,never()).insert(any(),any());
    }
    @Test void doesNotBlockUnrelatedRepairOfLegacyOrphan() {
        ObjectId id=new ObjectId();var orphan=new Document("_id",id).append("execution_id",new ObjectId()).append("user_id",1).append("rating",3);
        when(repository.find(AiResource.FEEDBACK,id)).thenReturn(orphan);
        when(repository.patch(eq(AiResource.FEEDBACK),eq(id),anyMap())).thenReturn(new Document(orphan).append("comment","updated"));
        assertEquals("updated",service.patch(AiResource.FEEDBACK,id.toHexString(),Map.of("comment","updated")).data().get("comment"));
        verify(repository,never()).find(eq(AiResource.AI_EXECUTION),any());
    }
    @Test void blocksParentDeletionWithDependents() {
        when(repository.exists(AiResource.AI_EXECUTION,"conversation_id",AiFixtures.REF)).thenReturn(true);
        assertEquals(409,assertThrows(ApiException.class,()->service.delete(AiResource.CONVERSATION,AiFixtures.REF.toHexString())).status.value());
        verify(repository,never()).delete(any(),any());
    }
    @Test void protectsReadOnlyEnvironments() throws Exception {
        var readOnly=new AiResourceService(repository,new MutationValidator(Validation.buildDefaultValidatorFactory().getValidator()),AiFixtures.mapper(),false);
        assertThrows(ApiException.class,()->readOnly.create(AiResource.CONVERSATION,AiFixtures.body(AiResource.CONVERSATION)));
        assertThrows(ApiException.class,()->readOnly.patch(AiResource.CONVERSATION,AiFixtures.REF.toHexString(),Map.of("summary","test")));
        assertThrows(ApiException.class,()->readOnly.delete(AiResource.CONVERSATION,AiFixtures.REF.toHexString()));
        verifyNoInteractions(repository);
    }
    @Test void handlesInvalidIdUnknownResourceMissingRecordAndBadPagination() {
        assertThrows(ApiException.class,()->service.find(AiResource.CONVERSATION,"bad-id"));
        assertThrows(ApiException.class,()->AiResource.fromRoute("users"));
        assertThrows(ApiException.class,()->service.find(AiResource.CONVERSATION,new ObjectId().toHexString()));
        assertThrows(ApiException.class,()->service.list(AiResource.CONVERSATION,Map.of("size","101")));
        assertThrows(ApiException.class,()->service.list(AiResource.RAG_DOCUMENT,Map.of("userId","1")));
    }
    @Test void persistsScopeAndFiltersConversationListByUserAndScope() {
        String scope="a".repeat(64);
        var body=new HashMap<String,Object>(AiFixtures.body(AiResource.CONVERSATION));
        body.put("accessScope",scope);
        assertEquals(scope,service.create(AiResource.CONVERSATION,body).data().get("accessScope"));
        when(repository.list(eq(AiResource.CONVERSATION),anyMap(),eq(0),eq(20))).thenReturn(List.of());
        service.list(AiResource.CONVERSATION,Map.of("userId","7","accessScope",scope));
        verify(repository).list(AiResource.CONVERSATION,Map.of("user_id",7,"access_scope",scope),0,20);
        assertThrows(ApiException.class,()->service.list(AiResource.CONVERSATION,Map.of("accessScope","invalid")));
        assertThrows(ApiException.class,()->service.create(AiResource.CONVERSATION,
                Map.of("userId",7,"status","ACTIVE","accessScope","invalid")));
    }

    @Test void mapsEmbeddedMemoryAndMixedMessageIdentifiers() {
        var body=new HashMap<String,Object>(AiFixtures.body(AiResource.CONVERSATION));
        body.put("messages",List.of(Map.of("sender","USER","content","synthetic","messageId","external-message"),Map.of("sender","AI","content","synthetic")));
        var response=service.create(AiResource.CONVERSATION,body);
        var messages=(List<Map<String,Object>>)response.data().get("messages");
        assertEquals("external-message",messages.get(0).get("messageId"));assertTrue(ObjectId.isValid((String)messages.get(1).get("messageId")));
        var memory=service.create(AiResource.USER_MEMORY,Map.of("userId",1,"memories",List.of(Map.of("type","fato","content","synthetic","importance",5))));
        var entries=(List<Map<String,Object>>)memory.data().get("memories");assertEquals(5.0,entries.getFirst().get("importance"));
    }
}
