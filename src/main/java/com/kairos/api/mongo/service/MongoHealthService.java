package com.kairos.api.mongo.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MongoHealthService {
    private final MongoTemplate mongo;

    public MongoHealthService(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public boolean isAvailable() {
        try {
            Document response = mongo.executeCommand(new Document("ping", 1));
            return response.get("ok") instanceof Number ok && ok.doubleValue() == 1.0;
        } catch (RuntimeException failure) {
            return false;
        }
    }
}
