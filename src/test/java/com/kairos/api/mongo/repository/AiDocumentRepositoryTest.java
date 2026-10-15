package com.kairos.api.mongo.repository;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.exception.ApiException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AiDocumentRepositoryTest {
    @Test void patchesAtomicallyWithoutReplacementUpsertOrUnknownFields() {
        var mongo=mock(MongoTemplate.class);when(mongo.collectionExists("conversations")).thenReturn(true);
        var repository=new AiDocumentRepository(mongo);var id=new ObjectId();var fields=new HashMap<String,Object>();fields.put("summary",null);
        repository.patch(AiResource.CONVERSATION,id,fields);
        var query=ArgumentCaptor.forClass(Query.class);var update=ArgumentCaptor.forClass(Update.class);var options=ArgumentCaptor.forClass(FindAndModifyOptions.class);
        verify(mongo).findAndModify(query.capture(),update.capture(),options.capture(),eq(Document.class),eq("conversations"));
        assertEquals(id,query.getValue().getQueryObject().get("_id"));assertEquals(new Document("$set",new Document("summary",null)),update.getValue().getUpdateObject());
        assertFalse(options.getValue().isUpsert());assertTrue(options.getValue().isReturnNew());
    }
    @Test void refusesImplicitCollectionCreation() {
        var mongo=mock(MongoTemplate.class);var repository=new AiDocumentRepository(mongo);
        assertThrows(ApiException.class,()->repository.insert(AiResource.CONVERSATION,new Document()));verify(mongo,never()).insert(any(Document.class),anyString());
    }
    @Test void boundsPaginationAndUsesTypedFilters() {
        var mongo=mock(MongoTemplate.class);var repository=new AiDocumentRepository(mongo);
        repository.list(AiResource.FEEDBACK,Map.of("user_id",1),2,10);
        var query=ArgumentCaptor.forClass(Query.class);verify(mongo).find(query.capture(),eq(Document.class),eq("feedback"));
        assertEquals(20,query.getValue().getSkip());assertEquals(11,query.getValue().getLimit());assertEquals(1,query.getValue().getQueryObject().get("user_id"));
    }
}
