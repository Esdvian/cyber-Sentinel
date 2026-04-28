package com.sentinel.agent.service;

/**
 * 提供商失败类型。
 */
public enum ProviderFailureType {
    RATE_LIMITED,
    QUOTA_EXHAUSTED,
    INSUFFICIENT_BALANCE,
    UPSTREAM_5XX,
    TIMEOUT,
    NON_RECOVERABLE
}
