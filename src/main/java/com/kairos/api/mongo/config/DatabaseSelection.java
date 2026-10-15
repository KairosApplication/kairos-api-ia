package com.kairos.api.mongo.config;

import com.mongodb.ConnectionString;

public final class DatabaseSelection {
    private DatabaseSelection() { }

    public static String resolve(ConnectionString connection, String configured) {
        String configuredDatabase = ExternalEnvValue.normalize(configured);
        String database = configuredDatabase.isBlank() ? connection.getDatabase() : configuredDatabase;
        if (database == null || database.isBlank()) {
            throw new IllegalStateException("Set MONGODB_DATABASE to the verified Kairos database name "
                    + "or include it in MONGODB_URI. No fallback to the test database is allowed.");
        }
        if (database.chars().anyMatch(value -> value == 32 || value == 47 || value == 92
                || value == 46 || value == 34 || value == 36 || value == 0)) {
            throw new IllegalStateException("MONGODB_DATABASE is not a valid database name.");
        }
        return database;
    }
}
