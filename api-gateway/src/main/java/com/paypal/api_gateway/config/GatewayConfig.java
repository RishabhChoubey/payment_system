package com.paypal.api_gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/auth/**")
                        .uri("http://localhost:8081"))
                .route("user-service-protected", r -> r
                        .path("/api/users/**")
                        // Do not apply gateway JWT filter here; let user-service validate JWT
                        .uri("http://localhost:8081"))
                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        // Let transaction-service perform auth/validation
                        .uri("http://localhost:8082"))
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .uri("http://localhost:8083"))
                .route("reward-service", r -> r
                        .path("/api/rewards/**")
                        .uri("http://localhost:8089"))
                .build();
    }
}
