package com.sentinel.agent.service;

import com.sentinel.agent.tools.GatewayManagementTools;
import com.sentinel.common.dto.ProviderRouteDefinition;
import com.sentinel.gateway.config.GatewayRouteProperties;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 主备 Provider 自动切换服务。
 */
@Service
public class ProviderFailoverService {

    private final GatewayManagementTools gatewayManagementTools;
    private final GatewayRouteProperties gatewayRouteProperties;
    private final ProviderFailureClassifier providerFailureClassifier;

    public ProviderFailoverService(GatewayManagementTools gatewayManagementTools,
                                   GatewayRouteProperties gatewayRouteProperties,
                                   ProviderFailureClassifier providerFailureClassifier) {
        this.gatewayManagementTools = gatewayManagementTools;
        this.gatewayRouteProperties = gatewayRouteProperties;
        this.providerFailureClassifier = providerFailureClassifier;
        this.gatewayManagementTools.registerProviders(gatewayRouteProperties.toProviderDefinitions());
    }

    public Optional<ProviderRouteDefinition> activeProvider() {
        return gatewayManagementTools.activeProviderDefinition()
                .or(() -> gatewayRouteProperties.findPrimary().map(GatewayRouteProperties.RouteDefinition::toProviderRouteDefinition));
    }

    public FailoverDecision handleFailure(Throwable throwable) {
        ProviderFailureType failureType = providerFailureClassifier.classify(throwable);
        if (!providerFailureClassifier.shouldFailover(failureType)) {
            return FailoverDecision.noop(failureType, "Failure is not eligible for automatic failover");
        }

        ProviderRouteDefinition active = activeProvider().orElseThrow(() -> new IllegalStateException("No active provider configured"));
        String fallbackProvider = active.fallbackProvider();
        if (fallbackProvider == null || fallbackProvider.isBlank()) {
            return FailoverDecision.noop(failureType, "No fallback provider configured for active provider: " + active.provider());
        }

        ProviderRouteDefinition fallback = gatewayManagementTools.findProvider(fallbackProvider)
                .or(() -> gatewayRouteProperties.findByProvider(fallbackProvider).map(GatewayRouteProperties.RouteDefinition::toProviderRouteDefinition))
                .orElseThrow(() -> new IllegalStateException("Fallback provider not found: " + fallbackProvider));

        gatewayManagementTools.switchKey(active.routeId(), fallback.provider());
        return FailoverDecision.switched(failureType, active, fallback);
    }

    public record FailoverDecision(
            boolean switched,
            ProviderFailureType failureType,
            String message,
            ProviderRouteDefinition fromProvider,
            ProviderRouteDefinition toProvider
    ) {
        public static FailoverDecision noop(ProviderFailureType failureType, String message) {
            return new FailoverDecision(false, failureType, message, null, null);
        }

        public static FailoverDecision switched(ProviderFailureType failureType,
                                                ProviderRouteDefinition fromProvider,
                                                ProviderRouteDefinition toProvider) {
            return new FailoverDecision(
                    true,
                    failureType,
                    "Provider failover executed from " + fromProvider.provider() + " to " + toProvider.provider(),
                    fromProvider,
                    toProvider
            );
        }
    }
}
