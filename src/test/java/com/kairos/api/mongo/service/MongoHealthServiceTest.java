package com.kairos.api.mongo.service;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MongoHealthServiceTest {
    @Test void reportsReachableMongoAndOnlySendsPing() {
        var mongo = mock(MongoTemplate.class);
        when(mongo.executeCommand(new Document("ping", 1))).thenReturn(new Document("ok", 1.0));
        assertTrue(new MongoHealthService(mongo).isAvailable());
        verify(mongo).executeCommand(new Document("ping", 1));
        verifyNoMoreInteractions(mongo);
    }
    @Test void handlesConnectionFailuresWithoutThrowingSecrets() {
        var mongo = mock(MongoTemplate.class);
        when(mongo.executeCommand(any(Document.class))).thenThrow(new IllegalStateException("private-uri"));
        assertFalse(new MongoHealthService(mongo).isAvailable());
    }
    @Test void handlesAbsentOrUnsuccessfulPingResult() {
        var mongo = mock(MongoTemplate.class);
        when(mongo.executeCommand(any(Document.class))).thenReturn(new Document());
        assertFalse(new MongoHealthService(mongo).isAvailable());
        when(mongo.executeCommand(any(Document.class))).thenReturn(new Document("ok", 0));
        assertFalse(new MongoHealthService(mongo).isAvailable());
    }
}
