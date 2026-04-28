package com.sentinel.bootstrap;

import com.sentinel.agent.service.ProviderFailoverService;
import com.sentinel.agent.service.ProviderFailoverService.FailoverDecision;
import com.sentinel.common.dto.ProviderRouteDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Map;

/**
 * 终端初步测试入口：验证主备 Provider 自动切换。
 */
@Component
@Order(100)
public class ProviderFailoverCliRunner implements CommandLineRunner {

    private final ProviderFailoverService providerFailoverService;
    private final WebClient.Builder webClientBuilder;
    private final boolean cliEnabled;

    public ProviderFailoverCliRunner(ProviderFailoverService providerFailoverService,
                                     WebClient.Builder webClientBuilder,
                                     @Value("${cyber-sentinel.cli.enabled:false}") boolean cliEnabled) {
        this.providerFailoverService = providerFailoverService;
        this.webClientBuilder = webClientBuilder;
        this.cliEnabled = cliEnabled;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!cliEnabled) {
            return;
        }

        System.out.println("[Cyber-Sentinel CLI] 已启动。输入 exit 退出。当前用于测试主备 Provider 自动切换。");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            ProviderRouteDefinition activeProvider = providerFailoverService.activeProvider()
                    .orElseThrow(() -> new IllegalStateException("No active provider available"));
            System.out.print("[active=" + activeProvider.provider() + "] > ");
            String input = reader.readLine();
            if (input == null || "exit".equalsIgnoreCase(input.trim())) {
                System.out.println("CLI 已退出。");
                break;
            }
            try {
                String result = invokeProvider(activeProvider, input);
                System.out.println(result);
            } catch (Throwable throwable) {
                FailoverDecision decision = providerFailoverService.handleFailure(throwable);
                if (decision.switched()) {
                    System.out.println("检测到主 Provider 失败类型=" + decision.failureType()
                            + "，已从 " + decision.fromProvider().provider()
                            + " 切换到 " + decision.toProvider().provider()
                            + "。请重新发送上一条消息继续测试。");
                } else {
                    System.out.println("请求失败，且未触发自动切换。failureType=" + decision.failureType()
                            + "，reason=" + decision.message());
                    if (throwable instanceof WebClientResponseException responseException) {
                        System.out.println("provider response body: " + responseException.getResponseBodyAsString());
                    } else {
                        System.out.println("provider error: " + throwable.getMessage());
                    }
                }
            }
        }
    }

    private String invokeProvider(ProviderRouteDefinition provider, String input) {
        WebClient webClient = webClientBuilder
                .baseUrl(provider.targetUri().toString())
                .build();

        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(Map.of(
                        "model", provider.model(),
                        "messages", new Object[]{Map.of("role", "user", "content", input)},
                        "stream", false
                ))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(45))
                .block();
    }
}
