package com.example.resilient_api.application.config;

import com.example.resilient_api.infrastructure.adapters.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Actuator - público
                        .pathMatchers("/actuator/**").permitAll()

                        // Swagger/OpenAPI - público
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()

                        // ===== ENDPOINTS ADMIN (solo isAdmin = true) =====
                        // Ver el bootcamp más popular - solo admin
                        .pathMatchers(HttpMethod.GET, "/metrics/bootcamp/most-popular").hasRole("ADMIN")

                        // ===== ENDPOINTS INTERNOS (sin autenticación de usuario) =====
                        // Registrar reporte de bootcamp - llamado internamente por capacity-api
                        .pathMatchers(HttpMethod.POST, "/metrics/bootcamp/report").permitAll()

                        // Por defecto: permitir todo lo demás
                        .anyExchange().permitAll()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}
