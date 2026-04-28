package com.sentinel.agent.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * 提供商失败分类器。
 */
@Component
public class ProviderFailureClassifier {

    public ProviderFailureType classify(Throwable throwable) {
        if (throwable == null) {
            return ProviderFailureType.NON_RECOVERABLE;
        }
        if (throwable instanceof TimeoutException || throwable instanceof SocketTimeoutException) {
            return ProviderFailureType.TIMEOUT;
        }
        if (throwable instanceof WebClientResponseException responseException) {
            HttpStatus statusCode = HttpStatus.resolve(responseException.getStatusCode().value());
            String body = responseException.getResponseBodyAsString().toLowerCase(Locale.ROOT);
            if (statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                return ProviderFailureType.RATE_LIMITED;
            }
            if (body.contains("quota exceeded")) {
                return ProviderFailureType.QUOTA_EXHAUSTED;
            }
            if (body.contains("insufficient balance") || body.contains("insufficient_quota")) {
                return ProviderFailureType.INSUFFICIENT_BALANCE;
            }
            if (statusCode != null && statusCode.is5xxServerError()) {
                return ProviderFailureType.UPSTREAM_5XX;
            }
        }
        String message = throwable.getMessage();
        if (message != null) {
            String normalized = message.toLowerCase(Locale.ROOT);
            if (normalized.contains("quota exceeded")) {
                return ProviderFailureType.QUOTA_EXHAUSTED;
            }
            if (normalized.contains("insufficient balance") || normalized.contains("insufficient quota")) {
                return ProviderFailureType.INSUFFICIENT_BALANCE;
            }
            if (normalized.contains("timeout") || normalized.contains("timed out")) {
                return ProviderFailureType.TIMEOUT;
            }
        }
        return ProviderFailureType.NON_RECOVERABLE;
    }

    public boolean shouldFailover(ProviderFailureType failureType) {
        return failureType == ProviderFailureType.RATE_LIMITED
                || failureType == ProviderFailureType.QUOTA_EXHAUSTED
                || failureType == ProviderFailureType.INSUFFICIENT_BALANCE
                || failureType == ProviderFailureType.UPSTREAM_5XX
                || failureType == ProviderFailureType.TIMEOUT;
    }
}
