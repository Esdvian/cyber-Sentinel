package com.sentinel.gateway.filter;

import com.sentinel.common.exception.GatewayErrorCode;
import com.sentinel.common.exception.GatewayException;
import com.sentinel.gateway.ratelimit.RedisRateLimitService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * AI 场景限流过滤器。
 */
public class AiRateLimitGatewayFilterFactory extends AbstractGatewayFilterFactory<AiRateLimitGatewayFilterFactory.Config> {

    private final RedisRateLimitService redisRateLimitService;

    public AiRateLimitGatewayFilterFactory(RedisRateLimitService redisRateLimitService) {
        super(Config.class);
        this.redisRateLimitService = Objects.requireNonNull(redisRateLimitService, "redisRateLimitService must not be null");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String key = config.keyResolver == null || config.keyResolver.isBlank()
                    ? exchange.getRequest().getRemoteAddress() + ":" + exchange.getRequest().getPath()
                    : config.keyResolver;
            return redisRateLimitService.isAllowed(key, config.replenishRate, config.burstCapacity)
                    .flatMap(allowed -> {
                        if (Boolean.TRUE.equals(allowed)) {
                            return chain.filter(exchange);
                        }
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    })
                    .onErrorResume(exception -> MonoErrorBridge.from(new GatewayException(
                            GatewayErrorCode.RATE_LIMITED,
                            "Rate limiting failed",
                            exception
                    )));
        };
    }

    public static class Config {
        private long replenishRate = 10;
        private long burstCapacity = 20;
        private String keyResolver;

        public long getReplenishRate() {
            return replenishRate;
        }

        public void setReplenishRate(long replenishRate) {
            this.replenishRate = replenishRate;
        }

        public long getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(long burstCapacity) {
            this.burstCapacity = burstCapacity;
        }

        public String getKeyResolver() {
            return keyResolver;
        }

        public void setKeyResolver(String keyResolver) {
            this.keyResolver = keyResolver;
        }
    }
}
