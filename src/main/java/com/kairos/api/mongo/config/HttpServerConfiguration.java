package com.kairos.api.mongo.config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods=false)
@ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
public class HttpServerConfiguration {
    @Bean WebServerFactoryCustomizer<TomcatServletWebServerFactory> asynchronousConnector() {
        // IOCP/NIO2 avoids the unavailable Unix-domain selector pipe in packaged Windows apps.
        return factory->factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
    }
}
