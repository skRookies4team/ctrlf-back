package com.ctrlf.chat.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자용 질문 로그 조회 응답 DTO
 * 
 * <p>AI 서버에서 FAQ 자동 생성을 위해 질문 로그를 조회할 때 사용합니다.</p>
 */
@Getter
@AllArgsConstructor
public class AdminMessageLogResponse {

    /** 질문 로그 목록 */
    private List<MessageLogItem> messages;

    /** 총 개수 */
    private Integer totalCount;

    /**
     * 질문 로그 항목
     */
    @Getter
    @AllArgsConstructor
    public static class MessageLogItem {
        /** 메시지 ID */
        private UUID id;

        /** 세션 ID */
        private UUID sessionId;

        /** 질문 내용 */
        private String content;

        /** 키워드 */
        private String keyword;

        /** 도메인 */
        private String domain;

        /** 사용자 ID (세션의 user_uuid) */
        private UUID userId;

        /** 생성 시각 */
        private Instant createdAt;
    }
}

