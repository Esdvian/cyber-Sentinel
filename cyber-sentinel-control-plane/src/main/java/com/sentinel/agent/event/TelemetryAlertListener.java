package com.sentinel.agent.event;

import com.sentinel.agent.service.TelemetryAnalysisService;
import com.sentinel.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 遥测事件监听器，负责触发控制面分析。
 */
@Component
public class TelemetryAlertListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryAlertListener.class);

    private final TelemetryAnalysisService telemetryAnalysisService;

    public TelemetryAlertListener(TelemetryAnalysisService telemetryAnalysisService) {
        this.telemetryAnalysisService = telemetryAnalysisService;
    }

    @EventListener
    public void onAlert(TelemetryAlertEvent event) {
        String payload = JsonUtils.toJson(event);
        String result = telemetryAnalysisService.analyze(payload);
        LOGGER.info("Telemetry alert processed, routeId={}, result={}", event.routeId(), result);
    }
}
