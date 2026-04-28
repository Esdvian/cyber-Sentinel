package com.sentinel.bootstrap;

import com.sentinel.gateway.config.GatewayRouteProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 应用统一启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.sentinel")
@EnableConfigurationProperties(GatewayRouteProperties.class)
public class CyberSentinelApplication {

    public static void main(String[] args) {
        SpringApplication.run(CyberSentinelApplication.class, args);
    }

    @Bean
    public ObservationRegistryCustomizer<?> observationRegistryCustomizer(MeterRegistry meterRegistry) {
        return registry -> registry.observationConfig().observationHandler(new MeterRegistryObservationHandlerAdapter(meterRegistry));
    }
}
