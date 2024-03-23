package com.apiGateWay.ApiGateWay;

import org.example.ModuleConfigurationApp;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Import({ModuleConfigurationApp.class})
public class AppConfig {

    @Value("${LB_SCRAPESERVICE_URL:http://scrape-service:8090}")
    private String lbScrapeServiceUrl;

    @Value("${ratelimiter-gateway}")
    private String ratelimiterGateway;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, Environment environment, RedisTemplate<String, Integer> redisTemplate) {
        String uri = !environment.matchesProfiles("prod","PROD") ? "lb://scrape-service" : lbScrapeServiceUrl;
        return builder.routes()
                .route("scrape-service", r -> r.path("/scrape/**")
                        .filters(f -> f.filter(new RateLimiter(redisTemplate,ratelimiterGateway)))
                        .uri(uri))
                .build();
    }

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        return template;
    }

}
