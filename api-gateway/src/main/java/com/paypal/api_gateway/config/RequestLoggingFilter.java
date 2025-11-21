package com.paypal.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        // ServerHttpRequest#getMethodValue may not be available depending on Spring version;
        // use getMethod() and handle null safely.
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
        HttpHeaders headers = exchange.getRequest().getHeaders();

        String auth = headers.getFirst("Authorization");
        String authPresent = auth != null ? "present" : "missing";
        logger.debug("Gateway incoming request: {} {} - Authorization: {}", method, path, authPresent);

        // Optionally log some headers for debugging (do not log full token)
        String preview = auth == null ? "" : (auth.length() > 64 ? auth.substring(0, 64) + "..." : auth);
        if (!preview.isEmpty()) {
            logger.debug("Authorization preview: {}", preview);
        }

        // Log other useful headers
        for (Map.Entry<String, java.util.List<String>> e : headers.entrySet()) {
            String key = e.getKey();
            if ("authorization" .equalsIgnoreCase(key) || "cookie".equalsIgnoreCase(key)) continue;
            logger.debug("Header {}: {}", key, e.getValue());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
