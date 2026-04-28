package com.sentinel.common.exception;

/**
 * 网关领域错误码。
 */
public enum GatewayErrorCode {
    INVALID_ROUTE,
    RATE_LIMITED,
    UPSTREAM_UNAVAILABLE,
    AUDIT_FAILURE,
    SELF_HEALING_REJECTED
}
