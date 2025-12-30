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
        /** 오늘 질문 수 */
        private Long todayQuestionCount;
        /** 평균 응답 시간 (밀리초) */
        private Long averageResponseTime;
        /** PII 감지 비율 (%) */
        private Double piiDetectionRate;
        /** 에러율 (%) */
        private Double errorRate;
        /** 최근 7일 질문 수 */
        private Long last7DaysQuestionCount;
        /** 활성 사용자 수 (최근 30일 기준) */
        private Long activeUserCount;
        /** 응답 만족도 (%) */
        private Double satisfactionRate;
        /** RAG 사용 비율 (%) */
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
     * 질문 수 · 에러율 추이 항목
     */
    @Getter
    @AllArgsConstructor
    public static class QuestionTrendItem {
        /** 기간 라벨 (예: "1주", "2주") */
        private String periodLabel;
        /** 질문 수 */
        private Long questionCount;
        /** 에러율 (%) */
        private Double errorRate;
    }

    /**
     * 질문 수 · 에러율 추이 응답
     */
    @Getter
    @AllArgsConstructor
    public static class QuestionTrendResponse {
        /** 기간 총 질문 수 */
        private Long totalQuestionCount;
        /** 구간당 평균 질문 수 */
        private Long averageQuestionCountPerPeriod;
        /** 평균 에러율 (%) */
        private Double averageErrorRate;
        private List<QuestionTrendItem> items;
    }

    /**
     * 도메인별 질문 비율 항목
     */
    @Getter
    @AllArgsConstructor
    public static class DomainRatioItem {
        /** 도메인 */
        private String domain;
        /** 도메인 이름 */
        private String domainName;
        /** 비율 (%) */
        private Double ratio;
    }

    /**
     * 도메인별 질문 비율 응답
     */
    @Getter
    @AllArgsConstructor
    public static class DomainRatioResponse {
        private List<DomainRatioItem> items;
    }
}

