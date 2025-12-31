package com.ctrlf.chat.telemetry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

/**
 * Telemetry 이벤트 수집 응답 DTO
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public final class TelemetryEventResponseDtos {
    private TelemetryEventResponseDtos() {}

    /**
     * 이벤트 수집 응답
     */
    @Getter
    @AllArgsConstructor
    public static class EventsResponse {
        /** 수신한 이벤트 수 */
        private Integer received;

        /** 수락된 이벤트 수 */
        private Integer accepted;

        /** 거부된 이벤트 수 */
        private Integer rejected;

        /** 에러 목록 */
        private List<EventError> errors;
    }

    /**
     * 이벤트 에러 정보
     */
    @Getter
    @AllArgsConstructor
    public static class EventError {
        /** 이벤트 ID */
        private String eventId;

        /** 에러 코드 */
        private String errorCode;

        /** 에러 메시지 */
        private String message;
    }
}

