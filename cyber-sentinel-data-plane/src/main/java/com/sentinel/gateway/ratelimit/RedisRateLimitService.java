package com.sentinel.gateway.ratelimit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * 基于 Redis Lua 的原子限流服务。
 */
public class RedisRateLimitService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    public RedisRateLimitService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setLocation(new ClassPathResource("lua/rate_limit_ai.lua"));
        this.rateLimitScript.setResultType(Long.class);
    }

    public Mono<Boolean> isAllowed(String key, long replenishRate, long burstCapacity) {
        long now = System.currentTimeMillis();
        List<String> keys = Collections.singletonList(key);
        return redisTemplate.execute(
                        rateLimitScript,
                        keys,
                        String.valueOf(now),
                        String.valueOf(replenishRate),
                        String.valueOf(burstCapacity)
                )
                .next()
                .map(result -> result != null && result > 0)
                .defaultIfEmpty(false);
    }
}
