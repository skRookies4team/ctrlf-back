package com.ctrlf.infra.ailog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI 로그 관련 DTO
 */
public final class AiLogDtos {
    private AiLogDtos() {}

    /**
     * AI 로그 Bulk 수신 요청
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkRequest {
        @NotEmpty
        private List<LogItem> logs;
    }

    /**
     * AI 로그 항목
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogItem {
        @NotNull
        private Instant createdAt;

        @NotNull
        private String userId;

        private String userRole;
        private String department;
        private String domain;
        private String route;
        private String modelName;
        private Boolean hasPiiInput;
        private Boolean hasPiiOutput;
        private Boolean ragUsed;
        private Integer ragSourceCount;
        private Long latencyMsTotal;
        private String errorCode;
        private String traceId;
        private String conversationId;
        private Integer turnId;
    }

    /**
     * AI 로그 Bulk 수신 응답
     */
    @Getter
    @AllArgsConstructor
    public static class BulkResponse {
        private Integer received;
        private Integer saved;
        private Integer failed;
        private List<ErrorItem> errors;
    }

    /**
     * 에러 항목
     */
    @Getter
    @AllArgsConstructor
    public static class ErrorItem {
        private Integer index;
        private String errorCode;
        private String message;
    }

    /**
     * 관리자 대시보드 로그 목록 조회 응답
     */
    @Getter
    @AllArgsConstructor
    public static class LogListItem {
        private UUID id;
        private String createdAt;
        private String userId;
        private String userRole;
        private String department;
        private String domain;
        private String route;
        private String modelName;
        private Boolean hasPiiInput;
        private Boolean hasPiiOutput;
        private Boolean ragUsed;
        private Integer ragSourceCount;
        private Long latencyMsTotal;
        private String errorCode;
    }

    /**
     * 페이지 응답
     */
    @Getter
    @AllArgsConstructor
    public static class PageResponse<T> {
        private List<T> content;
        private Long totalElements;
        private Integer totalPages;
        private Integer page;
        private Integer size;
    }
}

