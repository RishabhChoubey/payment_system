package com.paypal.api_gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GatewayFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for login and register endpoints
        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
            return chain.filter(exchange);
        }

        // Allow preflight CORS requests through without Authorization
        if (exchange.getRequest().getMethod() != null && exchange.getRequest().getMethod().name().equalsIgnoreCase("OPTIONS")) {
            logger.debug("Allowing preflight OPTIONS request {} through", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);

        // Debug log to help tracing header presence
        logger.debug("Incoming request {} with Authorization header: {}", path, authHeader != null ? "[present]" : "[missing]");

        // If auth is required but no token is present
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // Do not reject at gateway: allow the downstream service to handle authentication.
            logger.debug("No valid Authorization header for request {} — forwarding downstream for authentication", path);
            return chain.filter(exchange);
        }

        // Extract token for logging purposes only; do not block the request here.
        String token = authHeader.substring(BEARER_PREFIX.length());
        logger.debug("Authorization token present for request {} (token length={}) — forwarding downstream", path, token.length());

        // Ensure Authorization header is present on the proxied request by mutating
        // the ServerHttpRequest so downstream receives it reliably.
        try {
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(AUTH_HEADER, authHeader)
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            logger.warn("Failed to mutate request with Authorization header, forwarding original exchange", e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
