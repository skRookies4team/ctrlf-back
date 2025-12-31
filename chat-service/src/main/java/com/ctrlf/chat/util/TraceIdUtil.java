package com.ctrlf.chat.util;

import java.util.UUID;

/**
 * Trace ID 유틸리티
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public class TraceIdUtil {

    /**
     * 새로운 Trace ID 생성
     * 
     * @return UUID 형식의 Trace ID
     */
    public static UUID generateTraceId() {
        return UUID.randomUUID();
    }
}

