package com.paypal.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsWebFilter;

@Configuration
public class SecurityConfig {

    private final CorsWebFilter corsWebFilter;

    public SecurityConfig(CorsWebFilter corsWebFilter) {
        this.corsWebFilter = corsWebFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // The gateway should not enforce authentication for proxied API endpoints.
        // Downstream services (user-service, transaction-service, etc.) will validate JWTs.
        return http
            .cors(cors -> cors.disable()) // CORS is handled by CorsWebFilter
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(auth -> auth
                // Allow unauthenticated access through gateway; services handle auth
                .anyExchange().permitAll()
            )
            .build();
    }
}
