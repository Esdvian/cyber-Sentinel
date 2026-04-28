package com.sentinel.common.util;

import com.sentinel.common.exception.GatewayErrorCode;
import com.sentinel.common.exception.GatewayException;

/**
 * 轻量级 JSON 工具占位实现，后续在依赖解析稳定后再替换为 Jackson 实现。
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        return String.valueOf(value);
    }

    public static <T> T fromJson(String value, Class<T> targetType) {
        throw new GatewayException(
                GatewayErrorCode.AUDIT_FAILURE,
                "JSON deserialization is not enabled in the current lightweight skeleton for target type: " + targetType.getName()
        );
    }
}
