package com.sentinel.gateway.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Token 审计指标服务。
 */
public class TokenMetricsService {

    private final Map<String, LongAdder> routeTokenUsage = new ConcurrentHashMap<>();
    private MeterRegistry meterRegistry;

    public void bind(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementTokenUsage(String routeId, int tokens) {
        if (tokens <= 0) {
            return;
        }
        routeTokenUsage.computeIfAbsent(routeId, ignored -> new LongAdder()).add(tokens);
        if (meterRegistry != null) {
            Counter.builder("cyber_sentinel_token_usage_total")
                    .tag("routeId", routeId)
                    .register(meterRegistry)
                    .increment(tokens);
        }
    }

    public long currentUsage(String routeId) {
        LongAdder counter = routeTokenUsage.get(routeId);
        return counter == null ? 0L : counter.sum();
    }
}
