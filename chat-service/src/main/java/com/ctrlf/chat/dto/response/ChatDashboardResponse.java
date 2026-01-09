package com.ctrlf.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 챗봇 관리자 대시보드 응답 DTO
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public final class ChatDashboardResponse {
    private ChatDashboardResponse() {}

    /**
     * 대시보드 요약 통계 응답
     */
    @Getter
    @AllArgsConstructor
    public static class DashboardSummaryResponse {
        /** 기간 (today | 7d | 30d | 90d) */
        private String period;
        /** 부서 필터 (all 또는 dept_id) */
        private String dept;
        /** 오늘 질문 수 */
        private Long todayQuestionCount;
        /** 기간 내 질문 수 */
        private Long periodQuestionCount;
        /** 기간 내 일평균 질문 수 */
        private Long periodDailyAvgQuestionCount;
        /** 활성 사용자 수 */
        private Long activeUsers;
        /** 평균 응답 시간 (밀리초) */
        private Long avgLatencyMs;
        /** PII 감지 비율 */
        private Double piiDetectRate;
        /** 에러율 */
        private Double errorRate;
        /** 만족도 (like / (like + dislike)) */
        private Double satisfactionRate;
        /** 불만족도 (dislike / (like + dislike)) */
        private Double dislikeRate;
        /** RAG 사용 비율 */
        private Double ragUsageRate;
    }

    /**
     * 라우트별 질문 비율 항목
     */
    @Getter
    @AllArgsConstructor
    public static class RouteRatioItem {
        /** 라우트 타입 (RAG, LLM, INCIDENT, FAQ, OTHER) */
        private String routeType;
        /** 라우트 이름 */
        private String routeName;
        /** 비율 (%) */
        private Double ratio;
    }

    /**
     * 라우트별 질문 비율 응답
     */
    @Getter
    @AllArgsConstructor
    public static class RouteRatioResponse {
        private List<RouteRatioItem> items;
    }

    /**
     * 최근 많이 질문된 키워드 항목
     */
    @Getter
    @AllArgsConstructor
    public static class TopKeywordItem {
        /** 키워드 */
        private String keyword;
        /** 질문 횟수 */
        private Long questionCount;
    }

    /**
     * 최근 많이 질문된 키워드 Top 5 응답
     */
    @Getter
    @AllArgsConstructor
    public static class TopKeywordsResponse {
        private List<TopKeywordItem> items;
    }

    /**
     * 질문 수 · 에러율 추이 시리즈 항목
     */
    @Getter
    @AllArgsConstructor
    public static class TrendsSeriesItem {
        /** 버킷 시작일 (YYYY-MM-DD) */
        private String bucketStart;
        /** 질문 수 */
        private Long questionCount;
        /** 에러율 */
        private Double errorRate;
    }

    /**
     * 질문 수 · 에러율 추이 응답
     */
    @Getter
    @AllArgsConstructor
    public static class TrendsResponse {
        /** 버킷 타입 (day | week) */
        private String bucket;
        /** 시리즈 데이터 */
        private List<TrendsSeriesItem> series;
    }

    /**
     * 도메인별 질문 비율 항목
     */
    @Getter
    @AllArgsConstructor
    public static class DomainShareItem {
        /** 도메인 */
        private String domain;
        /** 도메인 라벨 */
        private String label;
        /** 질문 수 */
        private Long questionCount;
        /** 비율 (0~1) */
        private Double share;
    }

    /**
     * 도메인별 질문 비율 응답
     */
    @Getter
    @AllArgsConstructor
    public static class DomainShareResponse {
        private List<DomainShareItem> items;
    }
}

