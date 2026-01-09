package com.ctrlf.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 알림 응답 DTO
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public final class NotificationDtos {
    private NotificationDtos() {}

    /**
     * 알림 응답
     */
    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NotificationResponse {
        /** 알림 ID */
        private String id;
        
        /** 타임스탬프 (ISO 8601) */
        private String timestamp;
        
        /** 알림 타입 (info | warning | error | success) */
        private String type;
        
        /** 알림 메시지 */
        private String message;
        
        /** 읽음 여부 (SSE에서는 null, 폴링에서만 사용) */
        private Boolean read;
    }

    /**
     * 최근 알림 목록 응답
     */
    @Getter
    @AllArgsConstructor
    public static class RecentNotificationsResponse {
        private java.util.List<NotificationResponse> notifications;
    }
}

