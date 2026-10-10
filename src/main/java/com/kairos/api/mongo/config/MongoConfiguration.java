package com.kairos.api.mongo.config;

import com.kairos.api.mongo.exception.MongoConnectionException;
import com.kairos.api.mongo.service.MongoConnectionVerifier;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoConfigurationException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration(proxyBeanMethods = false)
public class MongoConfiguration {
    @Bean
    ConnectionString connectionString(@Value("${spring.mongodb.uri}") String uri) {
        try {
            String normalized = ExternalEnvValue.normalize(uri);
            if (normalized.isBlank()) {
                throw new IllegalArgumentException();
            }
            return new ConnectionString(normalized);
        } catch (MongoConfigurationException failure) {
            throw new MongoConnectionException("CONFIGURATION_OR_DNS");
        } catch (RuntimeException failure) {
            throw new MongoConnectionException("INVALID_URI");
        }
    }

    @Bean(destroyMethod = "close")
    MongoClient mongoClient(ConnectionString connection, MongoConnectionVerifier verifier,
            @Value("${app.mongodb.verify-on-startup:true}") boolean verify) {
        MongoClient client = null;
        try {
            var settings = MongoClientSettings.builder().applyConnectionString(connection)
                    .applyToClusterSettings(cluster -> cluster.serverSelectionTimeout(12, TimeUnit.SECONDS))
                    .applyToSocketSettings(socket -> socket.connectTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(8, TimeUnit.SECONDS))
                    .build();
            client = MongoClients.create(settings);
            if (verify) {
                verifier.verify(client);
            }
            return client;
        } catch (RuntimeException failure) {
            if (client != null) {
                client.close();
            }
            if (failure instanceof MongoConnectionException safe) {
                throw safe;
            }
            throw new MongoConnectionException("CONFIGURATION_OR_DNS");
        }
    }

    @Bean
    MongoTemplate mongoTemplate(MongoClient client, ConnectionString connection,
            @Value("${spring.mongodb.database:}") String database) {
        return new MongoTemplate(client, DatabaseSelection.resolve(connection, database));
    }
}
