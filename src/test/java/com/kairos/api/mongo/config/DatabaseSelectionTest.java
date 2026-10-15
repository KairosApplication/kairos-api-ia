package com.kairos.api.mongo.config;

import com.mongodb.ConnectionString;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseSelectionTest {
    @Test void usesDatabaseInUri() {
        assertEquals("kairos", DatabaseSelection.resolve(new ConnectionString("mongodb://localhost/kairos"), ""));
    }
    @Test void environmentDatabaseTakesPrecedence() {
        assertEquals("isolated", DatabaseSelection.resolve(new ConnectionString("mongodb://localhost/kairos"), " isolated "));
    }
    @Test void refusesImplicitTestDatabase() {
        var failure = assertThrows(IllegalStateException.class,
                () -> DatabaseSelection.resolve(new ConnectionString("mongodb://localhost"), ""));
        assertTrue(failure.getMessage().contains("MONGODB_DATABASE"));
    }
    @Test void rejectsInvalidDatabaseName() {
        assertThrows(IllegalStateException.class,
                () -> DatabaseSelection.resolve(new ConnectionString("mongodb://localhost"), "bad/name"));
    }
}
