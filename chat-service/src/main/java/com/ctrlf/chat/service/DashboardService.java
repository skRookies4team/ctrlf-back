package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.response.DashboardResponseDtos;

/**
 * 관리자 대시보드 서비스 인터페이스 (새 스펙)
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public interface DashboardService {

    /**
     * 챗봇 탭 상단 요약 카드 조회
     */
    DashboardResponseDtos.ChatSummaryResponse getChatSummary(
        String period,
        String dept,
        boolean refresh
    );

    /**
     * 질문 수·에러율 추이 조회
     */
    DashboardResponseDtos.ChatTrendsResponse getChatTrends(
        String period,
        String dept,
        String bucket,
        boolean refresh
    );

    /**
     * 도메인별 질문 비율 조회
     */
    DashboardResponseDtos.DomainShareResponse getDomainShare(
        String period,
        String dept,
        boolean refresh
    );

    /**
     * 보안 지표 조회
     */
    DashboardResponseDtos.SecurityMetricsResponse getSecurityMetrics(
        String period,
        String dept,
        boolean refresh
    );

    /**
     * 성능 지표 조회
     */
    DashboardResponseDtos.PerformanceMetricsResponse getPerformanceMetrics(
        String period,
        String dept,
        boolean refresh
    );
}

