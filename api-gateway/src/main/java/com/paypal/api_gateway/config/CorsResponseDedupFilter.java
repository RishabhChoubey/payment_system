package com.paypal.api_gateway.config;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class CorsResponseDedupFilter implements GlobalFilter, Ordered {

    private static final List<String> CORS_HEADERS = List.of(
        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
        HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();

            for (String headerName : CORS_HEADERS) {
                List<String> values = headers.get(headerName);
                if (values == null || values.size() <= 1) {
                    continue;
                }

                // Deduplicate while preserving order
                Set<String> unique = new LinkedHashSet<>(values);
                // If there are multiple identical values, reduce to single occurrence
                List<String> newValues = new ArrayList<>(unique);

                // Replace with single value(s) — keep only the first unique value to be safe
                headers.remove(headerName);
                if (!newValues.isEmpty()) {
                    headers.add(headerName, newValues.get(0));
                }
            }
        }));
    }

    @Override
    public int getOrder() {
        // Run after most filters; ORDER should be after NettyWriteResponseFilter (which is default  -  -1)
        // Use LOWEST_PRECEDENCE so it runs late and can inspect final response headers
        return Ordered.LOWEST_PRECEDENCE;
    }
}

