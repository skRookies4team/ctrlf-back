package com.ctrlf.chat.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 관리자 대시보드 응답 DTO (새 스펙)
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public final class DashboardResponseDtos {
    private DashboardResponseDtos() {}

    /**
     * 챗봇 탭 상단 요약 카드 응답
     */
    @Getter
    @AllArgsConstructor
    public static class ChatSummaryResponse {
        private String period;
        private String dept;
        private Long todayQuestionCount;
        private Long periodQuestionCount;
        private Long periodDailyAvgQuestionCount;
        private Long activeUsers;
        private Double avgLatencyMs;
        private Double piiDetectRate;
        private Double errorRate;
        private Double satisfactionRate; // null 가능
        private Double dislikeRate; // null 가능
        private Double ragUsageRate;
    }

    /**
     * 질문 수·에러율 추이 응답
     */
    @Getter
    @AllArgsConstructor
    public static class ChatTrendsResponse {
        private String bucket;
        private List<TrendItem> series;
    }

    /**
     * 추이 항목
     */
    @Getter
    @AllArgsConstructor
    public static class TrendItem {
        private String bucketStart;
        private Long questionCount;
        private Double errorRate;
    }

    /**
     * 도메인별 질문 비율 응답
     */
    @Getter
    @AllArgsConstructor
    public static class DomainShareResponse {
        private List<DomainShareItem> items;
    }

    /**
     * 도메인별 질문 비율 항목
     */
    @Getter
    @AllArgsConstructor
    public static class DomainShareItem {
        private String domain;
        private String label;
        private Long questionCount;
        private Double share;
    }

    /**
     * 보안 지표 응답
     */
    @Getter
    @AllArgsConstructor
    public static class SecurityMetricsResponse {
        private Long piiBlockCount;
        private Long externalDomainBlockCount;
        private List<PiiTrendItem> piiTrend;
    }

    /**
     * PII 추이 항목
     */
    @Getter
    @AllArgsConstructor
    public static class PiiTrendItem {
        private String bucketStart;
        private Double inputDetectRate;
        private Double outputDetectRate;
    }

    /**
     * 성능 지표 응답
     */
    @Getter
    @AllArgsConstructor
    public static class PerformanceMetricsResponse {
        private Double dislikeRate; // null 가능
        private Double repeatRate;
        private String repeatDefinition;
        private Long oosCount;
        private List<LatencyHistogramItem> latencyHistogram;
        private List<ModelLatencyItem> modelLatency;
    }

    /**
     * 지연 시간 히스토그램 항목
     */
    @Getter
    @AllArgsConstructor
    public static class LatencyHistogramItem {
        private String range;
        private Long count;
    }

    /**
     * 모델별 지연 시간 항목
     */
    @Getter
    @AllArgsConstructor
    public static class ModelLatencyItem {
        private String model;
        private Double avgLatencyMs;
    }
}

