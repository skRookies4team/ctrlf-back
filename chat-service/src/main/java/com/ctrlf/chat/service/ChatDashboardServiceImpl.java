package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.response.DashboardResponse;
import com.ctrlf.chat.repository.ChatFeedbackRepository;
import com.ctrlf.chat.repository.ChatMessageRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 대시보드 서비스 구현체
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatDashboardServiceImpl implements ChatDashboardService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatFeedbackRepository chatFeedbackRepository;

    // 라우트 타입 한글 매핑
    private static final Map<String, String> ROUTE_TYPE_NAMES = Map.of(
        "RAG", "RAG 기반 내부 규정",
        "LLM", "LLM 단독 답변",
        "FAQ", "FAQ 템플릿 응답",
        "INCIDENT", "Incident 신고 라우트",
        "OTHER", "기타/실험 라우트"
    );

    // 도메인 타입 한글 매핑
    private static final Map<String, String> DOMAIN_TYPE_NAMES = Map.of(
        "SECURITY", "규정 안내",
        "FAQ", "FAQ",
        "EDUCATION", "교육",
        "QUIZ", "퀴즈",
        "OTHER", "기타"
    );

    @Override
    public DashboardResponse.SummaryResponse getSummary(Integer periodDays, String department) {
        Instant now = Instant.now();
        Instant startDate = calculateStartDate(periodDays);
        Instant todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant todayEnd = todayStart.plus(1, ChronoUnit.DAYS);
        Instant last7DaysStart = now.minus(7, ChronoUnit.DAYS);

        // 오늘 질문 수
        Long todayQuestionCount = chatMessageRepository.countTodayQuestions(department);

        // 평균 응답 시간
        Double averageResponseTimeMs = chatMessageRepository.getAverageResponseTime(
            startDate, now, department
        );
        if (averageResponseTimeMs == null) {
            averageResponseTimeMs = 0.0;
        }

        // PII 감지 비율
        Double piiDetectionRate = chatMessageRepository.getPiiDetectionRate(
            startDate, now, department
        );
        if (piiDetectionRate == null) {
            piiDetectionRate = 0.0;
        }

        // 에러율
        Double errorRate = chatMessageRepository.getErrorRate(
            startDate, now, department
        );
        if (errorRate == null) {
            errorRate = 0.0;
        }

        // 최근 7일 질문 수
        Long last7DaysQuestionCount = chatMessageRepository.countUserMessagesByPeriod(
            last7DaysStart, now, department
        );
        if (last7DaysQuestionCount == null) {
            last7DaysQuestionCount = 0L;
        }

        // 활성 사용자 수
        Long activeUserCount = chatMessageRepository.countActiveUsers(
            startDate, now, department
        );
        if (activeUserCount == null) {
            activeUserCount = 0L;
        }

        // 응답 만족도
        Double satisfactionRate = chatFeedbackRepository.getSatisfactionRate(
            startDate, now, department
        );
        if (satisfactionRate == null) {
            satisfactionRate = 0.0;
        }

        // RAG 사용 비율
        List<Object[]> routeData = chatMessageRepository.getRouteRatio(
            startDate, now, department
        );
        double ragUsageRate = calculateRagUsageRate(routeData);

        return new DashboardResponse.SummaryResponse(
            todayQuestionCount,
            averageResponseTimeMs,
            piiDetectionRate,
            errorRate,
            last7DaysQuestionCount,
            activeUserCount,
            satisfactionRate,
            ragUsageRate
        );
    }

    @Override
    public DashboardResponse.RouteRatioResponse getRouteRatio(Integer periodDays, String department) {
        Instant now = Instant.now();
        Instant startDate = calculateStartDate(periodDays);

        List<Object[]> routeData = chatMessageRepository.getRouteRatio(
            startDate, now, department
        );

        // 전체 질문 수 계산
        long totalCount = routeData.stream()
            .mapToLong(row -> ((Number) row[1]).longValue())
            .sum();

        List<DashboardResponse.RouteRatioItem> items = new ArrayList<>();
        for (Object[] row : routeData) {
            String routeType = (String) row[0];
            Long questionCount = ((Number) row[1]).longValue();
            String routeName = ROUTE_TYPE_NAMES.getOrDefault(routeType, routeType);
            double ratio = totalCount > 0 ? (double) questionCount / totalCount * 100 : 0.0;

            items.add(new DashboardResponse.RouteRatioItem(
                routeType,
                routeName,
                ratio,
                questionCount
            ));
        }

        return new DashboardResponse.RouteRatioResponse(items);
    }

    @Override
    public DashboardResponse.TopKeywordsResponse getTopKeywords(Integer periodDays, String department) {
        Instant now = Instant.now();
        Instant startDate = calculateStartDate(periodDays);

        List<Object[]> keywordData = chatMessageRepository.getTopKeywords(
            startDate, now, department, 5
        );

        List<DashboardResponse.KeywordItem> items = new ArrayList<>();
        for (Object[] row : keywordData) {
            String keyword = (String) row[0];
            Long questionCount = ((Number) row[1]).longValue();

            items.add(new DashboardResponse.KeywordItem(
                keyword,
                questionCount
            ));
        }

        return new DashboardResponse.TopKeywordsResponse(items);
    }

    @Override
    public DashboardResponse.QuestionTrendResponse getQuestionTrend(Integer periodDays, String department) {
        Instant now = Instant.now();
        Instant startDate = calculateStartDate(periodDays);

        List<Object[]> weeklyData = chatMessageRepository.getWeeklyTrend(
            startDate, now, department
        );

        // 전체 질문 수 및 평균 계산
        long totalQuestionCount = 0;
        double totalErrorRate = 0.0;
        int weekCount = 0;

        List<DashboardResponse.WeeklyTrendItem> weeklyItems = new ArrayList<>();
        for (Object[] row : weeklyData) {
            Integer week = ((Number) row[0]).intValue();
            Long questionCount = ((Number) row[1]).longValue();
            Double errorRate = ((Number) row[2]).doubleValue();

            totalQuestionCount += questionCount;
            totalErrorRate += errorRate;
            weekCount++;

            weeklyItems.add(new DashboardResponse.WeeklyTrendItem(
                week,
                questionCount,
                errorRate
            ));
        }

        double averageQuestionCountPerPeriod = weekCount > 0 
            ? (double) totalQuestionCount / weekCount 
            : 0.0;
        double averageErrorRate = weekCount > 0 
            ? totalErrorRate / weekCount 
            : 0.0;

        return new DashboardResponse.QuestionTrendResponse(
            totalQuestionCount,
            averageQuestionCountPerPeriod,
            averageErrorRate,
            weeklyItems
        );
    }

    @Override
    public DashboardResponse.DomainRatioResponse getDomainRatio(Integer periodDays, String department) {
        Instant now = Instant.now();
        Instant startDate = calculateStartDate(periodDays);

        List<Object[]> domainData = chatMessageRepository.getDomainRatio(
            startDate, now, department
        );

        // 전체 질문 수 계산
        long totalCount = domainData.stream()
            .mapToLong(row -> ((Number) row[1]).longValue())
            .sum();

        List<DashboardResponse.DomainRatioItem> items = new ArrayList<>();
        for (Object[] row : domainData) {
            String domainType = (String) row[0];
            Long questionCount = ((Number) row[1]).longValue();
            String domainName = DOMAIN_TYPE_NAMES.getOrDefault(domainType, domainType);
            double ratio = totalCount > 0 ? (double) questionCount / totalCount * 100 : 0.0;

            items.add(new DashboardResponse.DomainRatioItem(
                domainType,
                domainName,
                ratio,
                questionCount
            ));
        }

        return new DashboardResponse.DomainRatioResponse(items);
    }

    /**
     * 기간 필터에 따른 시작 날짜 계산
     */
    private Instant calculateStartDate(Integer periodDays) {
        if (periodDays == null || periodDays <= 0) {
            periodDays = 30; // 기본값: 30일
        }
        return Instant.now().minus(periodDays, ChronoUnit.DAYS);
    }

    /**
     * RAG 사용 비율 계산
     */
    private double calculateRagUsageRate(List<Object[]> routeData) {
        if (routeData.isEmpty()) {
            return 0.0;
        }

        long totalCount = routeData.stream()
            .mapToLong(row -> ((Number) row[1]).longValue())
            .sum();

        long ragCount = routeData.stream()
            .filter(row -> "RAG".equals(row[0]))
            .mapToLong(row -> ((Number) row[1]).longValue())
            .sum();

        return totalCount > 0 ? (double) ragCount / totalCount * 100 : 0.0;
    }
}

