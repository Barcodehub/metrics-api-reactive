package com.example.resilient_api.infrastructure.adapters.security;

import com.example.resilient_api.domain.api.JwtPort;
import com.example.resilient_api.domain.model.JwtPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtPort jwtPort;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().toString();

        log.info("=== METRICS JWT FILTER === Processing: {} {}", method, path);

        // Skip authentication for public endpoints (actuator health checks)
        if (path.startsWith("/actuator")) {
            log.info("=== METRICS JWT FILTER === Actuator endpoint, skipping auth");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("=== METRICS JWT FILTER === Auth header present: {}", authHeader != null);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.info("=== METRICS JWT FILTER === No Bearer token, continuing without auth");
            return chain.filter(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        log.info("=== METRICS JWT FILTER === Validating JWT token");

        return jwtPort.validateAndExtractPayload(token)
                .flatMap(payload -> {
                    log.info("=== METRICS JWT FILTER === Token valid, authenticating user");
                    return authenticateUser(payload, exchange, chain);
                })
                .onErrorResume(ex -> {
                    log.error("=== METRICS JWT FILTER === Error validating JWT: {}", ex.getMessage());
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> authenticateUser(JwtPayload payload, ServerWebExchange exchange, WebFilterChain chain) {
        // Asignar rol basado en isAdmin
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(Boolean.TRUE.equals(payload.isAdmin()) ? "ROLE_ADMIN" : "ROLE_USER")
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(payload, null, authorities);

        // Store payload in exchange attributes for later use
        exchange.getAttributes().put("jwtPayload", payload);

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
