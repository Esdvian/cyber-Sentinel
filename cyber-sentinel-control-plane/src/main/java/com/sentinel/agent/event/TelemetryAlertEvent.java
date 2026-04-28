package com.sentinel.agent.event;

import java.time.Instant;
import java.util.Map;

/**
 * 遥测告警事件。
 */
public record TelemetryAlertEvent(
        String routeId,
        String provider,
        String severity,
        String summary,
        Map<String, Object> metrics,
        Instant occurredAt
) {
}
