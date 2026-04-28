package com.sentinel.agent.tools;

import com.sentinel.common.dto.ProviderRouteDefinition;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 提供给控制面智能体调用的网关管理工具。
 */
@Component
public class GatewayManagementTools {

    private final Map<String, String> providerBindings = new ConcurrentHashMap<>();
    private final Map<String, Integer> routeWeights = new ConcurrentHashMap<>();
    private final Map<String, ProviderRouteDefinition> providerRegistry = new ConcurrentHashMap<>();
    private final AtomicReference<String> activeProvider = new AtomicReference<>();

    public void registerProviders(Collection<ProviderRouteDefinition> definitions) {
        definitions.forEach(definition -> providerRegistry.put(definition.provider(), definition));
        definitions.stream()
                .filter(definition -> "primary".equalsIgnoreCase(definition.role()))
                .findFirst()
                .ifPresent(definition -> activeProvider.compareAndSet(null, definition.provider()));
    }

    public String switchKey(String routeId, String fallbackProvider) {
        providerBindings.put(routeId, fallbackProvider);
        activeProvider.set(fallbackProvider);
        return "Switched route " + routeId + " to provider " + fallbackProvider + " at " + Instant.now();
    }

    public String adjustWeight(String routeId, int weight) {
        routeWeights.put(routeId, weight);
        return "Adjusted weight for route " + routeId + " to " + weight;
    }

    public Optional<ProviderRouteDefinition> activeProviderDefinition() {
        return Optional.ofNullable(activeProvider.get())
                .map(providerRegistry::get);
    }

    public Optional<ProviderRouteDefinition> findProvider(String provider) {
        return Optional.ofNullable(providerRegistry.get(provider));
    }

    public String currentProvider() {
        return activeProvider.get();
    }

    public Map<String, String> currentBindings() {
        return Map.copyOf(providerBindings);
    }

    public Map<String, Integer> currentWeights() {
        return Map.copyOf(routeWeights);
    }
}
