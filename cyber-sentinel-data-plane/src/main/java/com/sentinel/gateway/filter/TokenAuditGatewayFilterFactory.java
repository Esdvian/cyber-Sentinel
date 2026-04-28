package com.sentinel.gateway.filter;

import com.knuddels.jtokkit.api.Encoding;
import com.sentinel.gateway.metrics.TokenMetricsService;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 对 SSE/流式响应执行 Token 审计。
 */
public class TokenAuditGatewayFilterFactory extends AbstractGatewayFilterFactory<TokenAuditGatewayFilterFactory.Config> {

    private final Encoding encoding;
    private final TokenMetricsService tokenMetricsService;

    public TokenAuditGatewayFilterFactory(Encoding encoding, TokenMetricsService tokenMetricsService) {
        super(Config.class);
        this.encoding = Objects.requireNonNull(encoding, "encoding must not be null");
        this.tokenMetricsService = Objects.requireNonNull(tokenMetricsService, "tokenMetricsService must not be null");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String routeId = exchange.getRequest().getId();
            var originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (!(body instanceof Flux<? extends DataBuffer> fluxBody)) {
                        return super.writeWith(body);
                    }
                    Flux<DataBuffer> transformedBody = fluxBody.map(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtilsEx.release(dataBuffer);
                        String chunkContent = new String(content, StandardCharsets.UTF_8);
                        int tokens = encoding.countTokens(chunkContent);
                        tokenMetricsService.incrementTokenUsage(routeId, tokens);
                        return bufferFactory.wrap(content);
                    });
                    return super.writeWith(transformedBody);
                }
            };
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        };
    }

    public static class Config {
    }
}
