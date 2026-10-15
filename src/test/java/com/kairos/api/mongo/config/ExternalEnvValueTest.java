package com.kairos.api.mongo.config;

import com.kairos.api.mongo.exception.MongoConnectionException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExternalEnvValueTest {
    @Test void acceptsQuotedEnvUriWithoutChangingItsContents() {
        var config = new MongoConfiguration();
        var single = config.connectionString("'mongodb://fake_user:fake_password@localhost/verified'");
        var doubleQuoted = config.connectionString("\"mongodb://localhost/verified\"");
        assertEquals("verified", single.getDatabase());
        assertEquals("fake_user", single.getCredential().getUserName());
        assertArrayEquals("fake_password".toCharArray(), single.getCredential().getPassword());
        assertEquals("verified", doubleQuoted.getDatabase());
    }
    @Test void acceptsQuotedDatabaseNameAndEmptyConfiguration() {
        var connection = new com.mongodb.ConnectionString("mongodb://localhost/from_uri");
        assertEquals("from_env", DatabaseSelection.resolve(connection, " 'from_env' "));
        assertEquals("from_uri", DatabaseSelection.resolve(connection, null));
    }
    @Test void invalidUriDoesNotLeakInputOrDriverCause() {
        var failure = assertThrows(MongoConnectionException.class,
                () -> new MongoConfiguration().connectionString("'private-password-invalid-uri'"));
        assertTrue(failure.getMessage().contains("INVALID_URI"));
        assertFalse(failure.getMessage().contains("private-password"));
        assertNull(failure.getCause());
    }
}
