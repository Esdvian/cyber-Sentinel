package com.sentinel.gateway.config;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import com.sentinel.gateway.metrics.TokenMetricsService;
import com.sentinel.gateway.ratelimit.RedisRateLimitService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

/**
 * 数据面自动配置。
 */
@Configuration
@EnableConfigurationProperties(GatewayRouteProperties.class)
public class DataPlaneAutoConfiguration {

    @Bean
    public Encoding cl100kEncoding() {
        return Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);
    }

    @Bean
    public TokenMetricsService tokenMetricsService() {
        return new TokenMetricsService();
    }

    @Bean
    public RedisRateLimitService redisRateLimitService(ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        return new RedisRateLimitService(reactiveStringRedisTemplate);
    }
}
