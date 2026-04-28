package com.sentinel.gateway.config;

import com.sentinel.common.dto.ProviderRouteDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 网关动态路由配置属性。
 */
@ConfigurationProperties(prefix = "cyber-sentinel.gateway")
public class GatewayRouteProperties {

    private final List<RouteDefinition> routes = new ArrayList<>();

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public Optional<RouteDefinition> findPrimary() {
        return routes.stream()
                .filter(RouteDefinition::isPrimary)
                .min(Comparator.comparingInt(RouteDefinition::getPriority));
    }

    public Optional<RouteDefinition> findByProvider(String provider) {
        return routes.stream()
                .filter(route -> route.getProvider().equalsIgnoreCase(provider))
                .findFirst();
    }

    public List<ProviderRouteDefinition> toProviderDefinitions() {
        return routes.stream()
                .map(RouteDefinition::toProviderRouteDefinition)
                .collect(Collectors.toList());
    }

    public static class RouteDefinition {

        private String routeId;
        private String provider;
        private URI targetUri;
        private String model;
        private String path;
        private String role = "secondary";
        private int priority = 100;
        private String fallbackProvider;
        private Map<String, String> metadata = Map.of();

        public String getRouteId() {
            return routeId;
        }

        public void setRouteId(String routeId) {
            this.routeId = routeId;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public URI getTargetUri() {
            return targetUri;
        }

        public void setTargetUri(URI targetUri) {
            this.targetUri = targetUri;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getFallbackProvider() {
            return fallbackProvider;
        }

        public void setFallbackProvider(String fallbackProvider) {
            this.fallbackProvider = fallbackProvider;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }

        public boolean isPrimary() {
            return "primary".equalsIgnoreCase(role);
        }

        public ProviderRouteDefinition toProviderRouteDefinition() {
            return new ProviderRouteDefinition(
                    routeId,
                    provider,
                    targetUri,
                    model,
                    path,
                    role,
                    priority,
                    fallbackProvider,
                    metadata
            );
        }
    }
}
