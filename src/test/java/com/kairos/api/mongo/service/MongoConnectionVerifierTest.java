package com.kairos.api.mongo.service;

import com.kairos.api.mongo.exception.MongoConnectionException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MongoConnectionVerifierTest {
    @Test void performsOnlyPing() {
        var client = mock(MongoClient.class);
        var database = mock(MongoDatabase.class);
        when(client.getDatabase("admin")).thenReturn(database);
        new MongoConnectionVerifier().verify(client);
        verify(database).runCommand(new Document("ping", 1));
        verifyNoMoreInteractions(database);
    }
    @Test void dropsDriverMessagesAndCauses() {
        var client = mock(MongoClient.class);
        var database = mock(MongoDatabase.class);
        when(client.getDatabase("admin")).thenReturn(database);
        when(database.runCommand(any(Document.class))).thenThrow(new MongoTimeoutException("private-password"));
        var failure = assertThrows(MongoConnectionException.class, () -> new MongoConnectionVerifier().verify(client));
        assertTrue(failure.getMessage().contains("NETWORK_OR_TLS_TIMEOUT"));
        assertFalse(failure.getMessage().contains("private-password"));
        assertNull(failure.getCause());
    }
}
