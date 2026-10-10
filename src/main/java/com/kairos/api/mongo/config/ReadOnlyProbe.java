package com.kairos.api.mongo.config;
import com.kairos.api.mongo.model.AiResource;
import com.kairos.api.mongo.service.AiResourceService;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Optional command-line probe; only bounded reads, no record content in logs. */
@Component
@ConditionalOnProperty(name="app.read-only-probe",havingValue="true")
public class ReadOnlyProbe implements ApplicationRunner {
    private final AiResourceService service;
    public ReadOnlyProbe(AiResourceService service) { this.service=service; }
    @Override public void run(ApplicationArguments arguments) {
        for (var resource:AiResource.values()) {
            try { service.list(resource,Map.of("size","2")); }
            catch (RuntimeException failure) { throw new IllegalStateException("Read-only model probe failed for "+resource.collection); }
        }
        LoggerFactory.getLogger(ReadOnlyProbe.class).info("Read-only model probe succeeded for {} collections.",AiResource.values().length);
    }
}
