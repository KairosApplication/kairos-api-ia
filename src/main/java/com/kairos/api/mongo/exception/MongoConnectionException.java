package com.kairos.api.mongo.exception;

/** Intentionally excludes driver messages and causes that may contain credentials. */
public final class MongoConnectionException extends IllegalStateException {
    public MongoConnectionException(String code) {
        super("MongoDB connection failed (" + code
                + "). Check DNS, TLS, Atlas Network Access and credentials; no database writes were attempted.");
    }
}
