package com.sentinel.agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * 自愈智能体入口。
 */
@AiService
public interface SelfHealingAgent {

    @SystemMessage({
            "你是工业级 AI 流量网关的控制塔。",
            "你需要根据监控指标识别上游提供商故障、流量异常与失败率激增。",
            "必要时建议调用网关管理工具切换提供商或调整路由权重。",
            "输出需保持简洁、可审计，并给出建议动作。"
    })
    String processTelemetryDataAndHeal(@UserMessage String metricsData);
}
