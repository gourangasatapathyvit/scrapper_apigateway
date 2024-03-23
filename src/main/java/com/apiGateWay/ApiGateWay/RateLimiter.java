package com.apiGateWay.ApiGateWay;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

public class RateLimiter implements GatewayFilter {
    private final RedisTemplate<String, Integer> redisTemplate;
    private final String ratelimiterGateway;

    public RateLimiter(RedisTemplate<String, Integer> redisTemplate, String ratelimiterGateway) {
        this.redisTemplate = redisTemplate;
        this.ratelimiterGateway = ratelimiterGateway;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String host = exchange.getRequest().getURI().getHost();
        if (exceedsRateLimit(host)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("Rate limit exceeded".getBytes())));
        }
        return chain.filter(exchange);
    }

    private boolean exceedsRateLimit(String ipAddress) {
        String key = "rate_limit:" + ipAddress;
        Long count = redisTemplate.opsForValue().increment(key, 1); // Increment key by 1
        if (count != null && count == 1) {
            // Set expiration time if the key is newly created
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        return count != null && count > Long.parseLong(ratelimiterGateway!=null?ratelimiterGateway:"5");
    }
}
