//package com.paypal.api_gateway.filter;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.UUID;
//
//@Component
//public class TraceIdGatewayFilter implements GlobalFilter, Ordered {
//    private static final Logger logger = LoggerFactory.getLogger(TraceIdGatewayFilter.class);
//
//@Override
//public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
//    String traceId = UUID.randomUUID().toString();
//    ServerWebExchange mutatedExchange = exchange.mutate()
//        .request(exchange.getRequest().mutate().header("X-Trace-Id", traceId).build())
//        .build();
//    logger.info("TraceId: {} | Path: {} | Routing to: {}", traceId, exchange.getRequest().getPath(), exchange.getAttribute("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR"));
//    return chain.filter(mutatedExchange);
//}
//
//    @Override
//    public int getOrder() {
//        return -1; // Run early
//    }
//}