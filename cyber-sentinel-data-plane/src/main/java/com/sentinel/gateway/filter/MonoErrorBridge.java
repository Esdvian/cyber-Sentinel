package com.sentinel.gateway.filter;

import reactor.core.publisher.Mono;

/**
 * 用于桥接响应式错误流。
 */
public final class MonoErrorBridge {

    private MonoErrorBridge() {
    }

    public static <T> Mono<T> from(Throwable throwable) {
        return Mono.error(throwable);
    }
}
