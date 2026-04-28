package com.sentinel.common.exception;

/**
 * 网关统一领域异常。
 */
public class GatewayException extends RuntimeException {

    private final GatewayErrorCode errorCode;

    public GatewayException(GatewayErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public GatewayException(GatewayErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public GatewayErrorCode getErrorCode() {
        return errorCode;
    }
}
