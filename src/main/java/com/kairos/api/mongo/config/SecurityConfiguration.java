package com.kairos.api.mongo.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
@Configuration(proxyBeanMethods=false)
@ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,@Value("${app.security.api-token:}") String token) throws Exception {
        return http.authorizeHttpRequests(auth->auth.requestMatchers(HttpMethod.GET,"/health").permitAll()
                        .requestMatchers("/api/v1/**").hasRole("API_ADMIN").anyRequest().denyAll())
                .addFilterBefore(new ApiTokenFilter(token),AnonymousAuthenticationFilter.class)
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable).logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(errors->errors.authenticationEntryPoint((request,response,failure)->{
                    response.setStatus(401); response.setContentType("application/json");
                    response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Valid API bearer token required.\"}");
                })).build();
    }
    @Bean UserDetailsService userDetailsService() {
        return username->{throw new UsernameNotFoundException("User password login is not supported by the observed AI database.");};
    }
}
