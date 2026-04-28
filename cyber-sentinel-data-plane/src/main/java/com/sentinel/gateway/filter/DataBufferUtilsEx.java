package com.sentinel.gateway.filter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

/**
 * 统一封装 DataBuffer 释放，便于后续切换实现。
 */
public final class DataBufferUtilsEx {

    private DataBufferUtilsEx() {
    }

    public static void release(DataBuffer dataBuffer) {
        DataBufferUtils.release(dataBuffer);
    }
}
