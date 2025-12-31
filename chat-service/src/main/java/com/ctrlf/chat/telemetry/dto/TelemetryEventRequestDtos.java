package com.ctrlf.chat.telemetry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Telemetry 이벤트 수집 요청 DTO
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public final class TelemetryEventRequestDtos {
    private TelemetryEventRequestDtos() {}

    /**
     * 이벤트 수집 요청
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventsRequest {
        /** 이벤트 소스 (예: "ai-gateway") */
        private String source;

        /** 전송 시각 (ISO-8601) */
        @JsonProperty("sentAt")
        private String sentAt;

        /** 이벤트 목록 */
        private List<Event> events;
    }

    /**
     * 개별 이벤트
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {
        /** 이벤트 ID (Idempotency key) */
        @JsonProperty("eventId")
        private UUID eventId;

        /** 이벤트 타입 (CHAT_TURN, FEEDBACK, SECURITY) */
        @JsonProperty("eventType")
        private String eventType;

        /** Trace ID */
        @JsonProperty("traceId")
        private UUID traceId;

        /** Conversation ID */
        @JsonProperty("conversationId")
        private String conversationId;

        /** Turn ID */
        @JsonProperty("turnId")
        private Integer turnId;

        /** User ID */
        @JsonProperty("userId")
        private String userId;

        /** Dept ID */
        @JsonProperty("deptId")
        private String deptId;

        /** 이벤트 발생 시각 (ISO-8601) */
        @JsonProperty("occurredAt")
        private String occurredAt;

        /** 이벤트 타입별 payload */
        private Map<String, Object> payload;
    }
}

