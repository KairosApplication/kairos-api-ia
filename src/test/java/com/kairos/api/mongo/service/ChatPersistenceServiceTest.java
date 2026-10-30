package com.kairos.api.mongo.service;

import com.kairos.api.mongo.AiFixtures;
import com.kairos.api.mongo.dto.request.RuntimeRequests.*;
import com.kairos.api.mongo.exception.ApiException;
import com.mongodb.client.result.UpdateResult;
import java.util.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatPersistenceServiceTest {
    MongoTemplate mongo;
    ChatPersistenceService service;
    final ObjectId id=AiFixtures.REF;
    Document conversation() { return new Document("_id",id).append("user_id",7).append("status","ACTIVE").append("messages",List.of()); }
    @BeforeEach void setup() throws Exception {
        mongo=mock(MongoTemplate.class);
        service=new ChatPersistenceService(mongo,AiFixtures.mapper(),true,900);
        when(mongo.collectionExists(anyString())).thenReturn(true);
    }
    @Test void appendsPairAtomicallyWithOwnershipIdempotencyAndLease() {
        when(mongo.findAndModify(any(Query.class),any(Update.class),any(FindAndModifyOptions.class),eq(Document.class),eq("conversations"))).thenReturn(conversation());
        service.appendTurn(7,id.toHexString(),new Turn("request-1","Question","Answer"),"request-1");
        var query=ArgumentCaptor.forClass(Query.class);var update=ArgumentCaptor.forClass(Update.class);
        verify(mongo).findAndModify(query.capture(),update.capture(),any(FindAndModifyOptions.class),eq(Document.class),eq("conversations"));
        assertEquals(7,query.getValue().getQueryObject().get("user_id"));
        assertEquals("request-1",query.getValue().getQueryObject().get("runtime_lease_owner"));
        assertTrue(query.getValue().getQueryObject().containsKey("messages.message_id"));
        assertTrue(update.getValue().getUpdateObject().containsKey("$push"));
        assertFalse(update.getValue().getUpdateObject().get("$set",Document.class).containsKey("messages"));
    }
    @Test void repeatedTurnDoesNotAppendAgain() {
        var current=conversation();
        current.put("messages",List.of(new Document("message_id","r:USER").append("content","Q"),
                new Document("message_id","r:AI").append("content","A")));
        when(mongo.findOne(any(Query.class),eq(Document.class),eq("conversations"))).thenReturn(current);
        assertEquals(id.toHexString(),service.appendTurn(7,id.toHexString(),new Turn("r","Q","A"),null).id());
        assertThrows(ApiException.class,()->service.appendTurn(7,id.toHexString(),new Turn("r","Changed","A"),null));
    }
    @Test void storesSourcesOnlyOnAssistantMessageAndChecksIdempotentMetadata() {
        when(mongo.findAndModify(any(Query.class),any(Update.class),any(FindAndModifyOptions.class),eq(Document.class),eq("conversations")))
                .thenAnswer(call -> {
                    var update=call.<Update>getArgument(1).getUpdateObject();
                    var modifiers=(Update.Modifiers)update.get("$push",Document.class).get("messages");
                    var each=modifiers.getModifiers().stream().filter(modifier->modifier.getKey().equals("$each")).findFirst().orElseThrow().getValue();
                    var messages=each instanceof List<?> list ? list : Arrays.asList((Object[])each);
                    assertFalse(((Document)messages.get(0)).containsKey("sources"));
                    assertEquals(List.of("pdf","memory"),((Document)messages.get(1)).get("sources"));
                    return conversation();
                });
        service.appendTurn(7,id.toHexString(),new Turn("r","Q","A",List.of("pdf","memory","pdf")),null);
        reset(mongo);
        when(mongo.collectionExists(anyString())).thenReturn(true);
        var stored=conversation();
        stored.put("messages",List.of(new Document("message_id","r:USER").append("content","Q"),
                new Document("message_id","r:AI").append("content","A").append("sources",List.of("pdf"))));
        when(mongo.findOne(any(Query.class),eq(Document.class),eq("conversations"))).thenReturn(stored);
        assertEquals(409,assertThrows(ApiException.class,()->service.appendTurn(7,id.toHexString(),
                new Turn("r","Q","A",List.of("database")),null)).status.value());
    }
    @Test void summaryUsesPreviousVersionAndDoesNotOvershootHistory() {
        when(mongo.findAndModify(any(Query.class),any(Update.class),any(FindAndModifyOptions.class),eq(Document.class),eq("conversations"))).thenReturn(conversation());
        service.summarize(7,id.toHexString(),new Summary("summary",null,0,4),null);
        var query=ArgumentCaptor.forClass(Query.class);
        verify(mongo).findAndModify(query.capture(),any(Update.class),any(FindAndModifyOptions.class),eq(Document.class),eq("conversations"));
        var filter=query.getValue().getQueryObject();
        assertTrue(filter.containsKey("summary"));
        assertTrue(filter.get("$expr").toString().contains("$summarized_count"));
        assertTrue(filter.get("$expr").toString().contains("$size"));
        assertThrows(ApiException.class,()->service.summarize(7,id.toHexString(),new Summary("s",null,0,3),null));
        assertThrows(ApiException.class,()->service.summarize(7,id.toHexString(),new Summary("s",null,4,2),null));
    }
    @Test void staleSummaryReturnsConflictWithoutRetryingWrite() {
        when(mongo.findOne(any(Query.class),eq(Document.class),eq("conversations"))).thenReturn(conversation());
        var error=assertThrows(ApiException.class,()->service.summarize(7,id.toHexString(),new Summary("s",null,0,4),null));
        assertEquals(409,error.status.value());
        verify(mongo,times(1)).findAndModify(any(Query.class),any(Update.class),any(FindAndModifyOptions.class),eq(Document.class),eq("conversations"));
    }
    @Test void ownerFilterHidesOtherUsersConversation() {
        var error=assertThrows(ApiException.class,()->service.find(7,id.toHexString()));
        assertEquals(404,error.status.value());
        var query=ArgumentCaptor.forClass(Query.class);
        verify(mongo).findOne(query.capture(),eq(Document.class),eq("conversations"));
        assertEquals(7,query.getValue().getQueryObject().get("user_id"));
    }
    @Test void unavailableLeaseConflictsAndReleaseCannotUnlockAnotherOwner() {
        when(mongo.updateFirst(any(Query.class),any(Update.class),eq("conversations"))).thenReturn(UpdateResult.acknowledged(0,0L,null));
        when(mongo.findOne(any(Query.class),eq(Document.class),eq("conversations"))).thenReturn(conversation());
        assertEquals(409,assertThrows(ApiException.class,()->service.acquire(7,id.toHexString(),new Lease("owner"))).status.value());
        service.release(7,id.toHexString(),new Lease("owner"));
        var queries=ArgumentCaptor.forClass(Query.class);
        verify(mongo,times(2)).updateFirst(queries.capture(),any(Update.class),eq("conversations"));
        assertEquals("owner",queries.getAllValues().get(1).getQueryObject().get("runtime_lease_owner"));
    }
    @Test void existingActiveConversationIsReadWithoutEnablingWrites() throws Exception {
        service=new ChatPersistenceService(mongo,AiFixtures.mapper(),false,900);
        when(mongo.findOne(any(Query.class),eq(Document.class),eq("conversations"))).thenReturn(conversation());
        assertEquals(id.toHexString(),service.active(7).id());
        assertEquals(403,assertThrows(ApiException.class,()->service.appendTurn(7,id.toHexString(),new Turn("r","Q","A"),null)).status.value());
        verify(mongo,never()).findAndModify(any(Query.class),any(Update.class),any(FindAndModifyOptions.class),eq(Document.class),anyString());
    }
    @Test void appendsMemoriesWithStableUpsertAndPreservesExistingArray() {
        var stored=new Document("_id",id).append("user_id",7).append("memories",List.of());
        when(mongo.findAndModify(any(Query.class),any(Update.class),any(FindAndModifyOptions.class),eq(Document.class),eq("user_memory"))).thenReturn(stored);
        service.appendMemories(7,new Memories(List.of(new com.kairos.api.mongo.dto.request.MemoryRequest(null,"fato","Conteúdo",3.0,null,null,"a".repeat(64)))));
        var query=ArgumentCaptor.forClass(Query.class);var update=ArgumentCaptor.forClass(Update.class);
        verify(mongo).findAndModify(query.capture(),update.capture(),any(FindAndModifyOptions.class),eq(Document.class),eq("user_memory"));
        assertTrue(update.getValue().toString().contains("access_scope"));
        assertTrue(update.getValue().toString().contains("a".repeat(64)));
        assertEquals(new ObjectId("ffffffffffffffff00000007"),query.getValue().getQueryObject().get("_id"));
        assertTrue(update.getValue().getUpdateObject().containsKey("$push"));
        assertFalse(update.getValue().getUpdateObject().get("$set",Document.class).containsKey("memories"));
    }
}
