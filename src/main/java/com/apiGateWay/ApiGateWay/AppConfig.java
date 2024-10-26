package com.apiGateWay.ApiGateWay;

import org.example.ModuleConfigurationApp;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Import({ModuleConfigurationApp.class})
public class AppConfig {

    @Value("${lbScrapeServiceUrl}")
    private String lbScrapeServiceUrl;

    @Value("${ratelimiter-gateway}")
    private String ratelimiterGateway;

    @Value("${spring.data.redis}")
    private String redisServe;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, Environment environment, RedisTemplate<String, Integer> redisTemplate) {
        String uri = lbScrapeServiceUrl;
        return builder.routes()
                .route("scrape-service", r -> r.path("/scrape/**")
                        .filters(f -> f.filter(new RateLimiter(redisTemplate, ratelimiterGateway)))
                        .uri(uri))
                .build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisServe, Integer.parseInt(redisPort));
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        return template;
    }

}
