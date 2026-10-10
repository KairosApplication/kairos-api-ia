package com.kairos.api.mongo;

import com.kairos.api.mongo.service.MongoHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.config.import=", "spring.mongodb.uri=mongodb://127.0.0.1:1/isolated_test",
        "spring.mongodb.database=isolated_test", "app.mongodb.verify-on-startup=false"
})
class HealthHttpIntegrationTest {
    @Autowired WebApplicationContext context;
    @MockitoBean MongoHealthService health;
    MockMvc http;

    @BeforeEach void setup() {
        http = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void contextStartsAndSerializesHealthContract() throws Exception {
        when(health.isAvailable()).thenReturn(true);
        http.perform(get("/health")).andExpect(status().isOk()).andExpect(content().json("{\"status\":\"UP\"}"));
    }
    @Test void unhealthyMongoReturns503WithNoDetails() throws Exception {
        when(health.isAvailable()).thenReturn(false);
        http.perform(get("/health")).andExpect(status().isServiceUnavailable())
                .andExpect(content().json("{\"status\":\"DOWN\"}"));
    }
    @Test void businessEndpointsAreNotExposedWithoutMapping() throws Exception {
        http.perform(get("/api/v1/users/list")).andExpect(status().isUnauthorized());
        http.perform(post("/api/v1/users/registration")).andExpect(status().isUnauthorized());
    }
}
