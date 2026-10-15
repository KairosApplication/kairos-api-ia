package com.kairos.api.mongo.config;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
final class ApiTokenFilter extends OncePerRequestFilter {
    private final byte[] expected;
    ApiTokenFilter(String token) {
        String normalized=ExternalEnvValue.normalize(token);
        if (!normalized.isEmpty() && normalized.length()<32) throw new IllegalStateException("API_ACCESS_TOKEN must contain at least 32 characters.");
        expected=normalized.getBytes(StandardCharsets.UTF_8);
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        String header=request.getHeader("Authorization");
        if (expected.length>0 && header!=null && header.startsWith("Bearer ")
                && MessageDigest.isEqual(expected,header.substring(7).getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated("api-service",null,
                    List.of(new SimpleGrantedAuthority("ROLE_API_ADMIN"))));
        }
        chain.doFilter(request,response);
    }
}
