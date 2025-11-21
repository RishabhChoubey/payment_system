package com.paypal.api_gateway.config;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

@Component
public class CorsAddHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
            HttpHeaders responseHeaders = exchange.getResponse().getHeaders();

            String origin = requestHeaders.getOrigin();
            if (origin != null && !origin.isEmpty()) {
                // Echo back the Origin value (safest approach when allowCredentials=true)
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                // Keep methods and headers consistent with CorsConfig
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS");
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                responseHeaders.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Access-Control-Allow-Origin");
            }
        }));
    }

    @Override
    public int getOrder() {
        // Run late so other filters can add headers first
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}

