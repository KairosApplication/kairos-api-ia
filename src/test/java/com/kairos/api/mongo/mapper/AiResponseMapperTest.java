package com.kairos.api.mongo.mapper;
import com.kairos.api.mongo.AiFixtures;
import com.kairos.api.mongo.model.AiResource;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AiResponseMapperTest {
    @Test void readsMixedDatesMissingFieldsAndPolymorphicEmbeddedIdsWithoutChangingBson() throws Exception {
        var id=new ObjectId();var bsonId=new ObjectId();var date=Date.from(Instant.parse("2026-10-10T10:00:00Z"));
        var document=new Document("_id",id).append("user_id",1).append("status","ACTIVE").append("created_at",date)
                .append("updated_at","2026-10-10T11:00:00").append("unknown","hidden")
                .append("messages",List.of(new Document("message_id",bsonId).append("sender","USER").append("content","synthetic").append("timestamp",date),
                        new Document("message_id","external").append("sender","AI").append("content","synthetic").append("timestamp","2026-10-10T10:01:00Z")));
        var response=AiFixtures.mapper().response(AiResource.CONVERSATION,document);
        assertEquals(id.toHexString(),response.id());assertEquals(date.toInstant().toString(),response.data().get("createdAt"));
        assertEquals("2026-10-10T11:00:00",response.data().get("updatedAt"));assertNull(response.data().get("summary"));assertFalse(response.data().containsKey("unknown"));
        assertEquals(date,document.get("created_at"));assertEquals(bsonId,((List<Document>)document.get("messages")).getFirst().get("message_id"));
    }
}
