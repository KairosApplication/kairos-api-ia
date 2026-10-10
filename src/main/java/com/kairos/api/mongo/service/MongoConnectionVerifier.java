package com.kairos.api.mongo.service;

import com.kairos.api.mongo.exception.MongoConnectionException;
import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.springframework.stereotype.Service;

@Service
public class MongoConnectionVerifier {
    public void verify(MongoClient client) {
        try {
            client.getDatabase("admin").runCommand(new Document("ping", 1));
        } catch (MongoSecurityException failure) {
            throw new MongoConnectionException("AUTHENTICATION");
        } catch (MongoConfigurationException failure) {
            throw new MongoConnectionException("CONFIGURATION_OR_DNS");
        } catch (MongoTimeoutException failure) {
            throw new MongoConnectionException("NETWORK_OR_TLS_TIMEOUT");
        } catch (RuntimeException failure) {
            throw new MongoConnectionException("CONNECTION");
        }
    }
}
