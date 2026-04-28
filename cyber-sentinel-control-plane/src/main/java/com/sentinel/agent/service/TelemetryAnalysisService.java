package com.sentinel.agent.service;

import org.springframework.stereotype.Service;

/**
 * 负责对监控事件进行规整，并交由智能体处理。
 */
@Service
public class TelemetryAnalysisService {

    private final SelfHealingAgent selfHealingAgent;

    public TelemetryAnalysisService(SelfHealingAgent selfHealingAgent) {
        this.selfHealingAgent = selfHealingAgent;
    }

    public String analyze(String telemetryPayload) {
        return selfHealingAgent.processTelemetryDataAndHeal(telemetryPayload);
    }
}
