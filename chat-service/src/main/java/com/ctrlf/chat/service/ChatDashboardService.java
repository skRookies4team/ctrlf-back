package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.response.ChatDashboardResponse;

/**
 * 챗봇 관리자 대시보드 서비스 인터페이스
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public interface ChatDashboardService {

    /**
     * 대시보드 요약 통계 조회
     * 
     * @param period 기간 (today | 7d | 30d | 90d)
     * @param dept 부서 필터 (all 또는 dept_id)
     * @param refresh 캐시 무시 여부
     * @return 요약 통계 응답
     */
    ChatDashboardResponse.DashboardSummaryResponse getDashboardSummary(
        String period,
        String dept,
        Boolean refresh
    );

    /**
     * 질문 수 · 에러율 추이 조회
     * 
     * @param period 기간 (today | 7d | 30d | 90d)
     * @param dept 부서 필터 (all 또는 dept_id)
     * @param bucket 버킷 타입 (day | week)
     * @param refresh 캐시 무시 여부
     * @return 추이 응답
     */
    ChatDashboardResponse.TrendsResponse getTrends(
        String period,
        String dept,
        String bucket,
        Boolean refresh
    );

    /**
     * 도메인별 질문 비율 조회
     * 
     * @param period 기간 (today | 7d | 30d | 90d)
     * @param dept 부서 필터 (all 또는 dept_id)
     * @param refresh 캐시 무시 여부
     * @return 도메인별 질문 비율 응답
     */
    ChatDashboardResponse.DomainShareResponse getDomainShare(
        String period,
        String dept,
        Boolean refresh
    );
}

