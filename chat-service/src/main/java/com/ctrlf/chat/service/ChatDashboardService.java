package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.response.DashboardResponse;

/**
 * 관리자 대시보드 서비스 인터페이스
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public interface ChatDashboardService {

    /**
     * 대시보드 요약 통계 조회
     * 
     * @param periodDays 기간 (일수, 7/30/90)
     * @param department 부서 필터 (null이면 전체)
     * @return 요약 통계 응답
     */
    DashboardResponse.SummaryResponse getSummary(Integer periodDays, String department);

    /**
     * 라우트별 질문 비율 조회
     * 
     * @param periodDays 기간 (일수, 7/30/90)
     * @param department 부서 필터 (null이면 전체)
     * @return 라우트별 비율 응답
     */
    DashboardResponse.RouteRatioResponse getRouteRatio(Integer periodDays, String department);

    /**
     * 최근 많이 질문된 키워드 Top 5 조회
     * 
     * @param periodDays 기간 (일수, 7/30/90)
     * @param department 부서 필터 (null이면 전체)
     * @return 키워드 Top 5 응답
     */
    DashboardResponse.TopKeywordsResponse getTopKeywords(Integer periodDays, String department);

    /**
     * 질문 수 · 에러율 추이 조회
     * 
     * @param periodDays 기간 (일수, 7/30/90)
     * @param department 부서 필터 (null이면 전체)
     * @return 질문/에러율 추이 응답
     */
    DashboardResponse.QuestionTrendResponse getQuestionTrend(Integer periodDays, String department);

    /**
     * 도메인별 질문 비율 조회
     * 
     * @param periodDays 기간 (일수, 7/30/90)
     * @param department 부서 필터 (null이면 전체)
     * @return 도메인별 비율 응답
     */
    DashboardResponse.DomainRatioResponse getDomainRatio(Integer periodDays, String department);
}

