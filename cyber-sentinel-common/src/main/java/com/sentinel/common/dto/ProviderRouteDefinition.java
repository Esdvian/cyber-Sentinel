package com.sentinel.common.dto;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * 提供商路由定义，保持为纯 DTO，避免引入 Spring 依赖。
 */
public record ProviderRouteDefinition(
        String routeId,
        String provider,
        URI targetUri,
        String model,
        String path,
        String role,
        int priority,
        String fallbackProvider,
        Map<String, String> metadata
) {

    public ProviderRouteDefinition {
        Objects.requireNonNull(routeId, "routeId must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(targetUri, "targetUri must not be null");
        Objects.requireNonNull(model, "model must not be null");
        path = path == null ? "/**" : path;
        role = role == null ? "secondary" : role;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
