package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.response.ChatDashboardResponse;
import com.ctrlf.chat.repository.ChatFeedbackRepository;
import com.ctrlf.chat.repository.ChatMessageRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 챗봇 관리자 대시보드 서비스 구현체
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

    private static final Map<String, String> ROUTE_NAME_MAP = Map.of(
        "RAG", "RAG 기반 내부 규정",
        "LLM", "LLM 단독 답변",
        "INCIDENT", "Incident 신고 라우트",
        "FAQ", "FAQ 템플릿 응답",
        "OTHER", "기타/실험 라우트"
    );

    private static final Map<String, String> DOMAIN_NAME_MAP = Map.of(
        "SECURITY", "규정 안내",
        "POLICY", "규정 안내",
        "FAQ", "FAQ",
        "EDUCATION", "교육",
        "QUIZ", "퀴즈",
        "OTHER", "기타"
    );

    @Override
    public ChatDashboardResponse.DashboardSummaryResponse getDashboardSummary(
        Integer periodDays,
        String department
    ) {
        // 기본값: 최근 30일
        if (periodDays == null) {
            periodDays = 30;
        }

        Instant startDate = calculateStartDate(periodDays);
        Instant todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();

        // 오늘 질문 수
        Long todayQuestionCount = chatMessageRepository.countTodayQuestions(department);

        // 평균 응답 시간 (밀리초)
        Long averageResponseTime = chatMessageRepository.getAverageResponseTime(startDate, department);
        if (averageResponseTime == null) {
            averageResponseTime = 0L;
        }

        // PII 감지 비율 (%)
        Double piiDetectionRate = chatMessageRepository.getPiiDetectionRate(startDate, department);
        if (piiDetectionRate == null) {
            piiDetectionRate = 0.0;
        }

        // 에러율 (%)
        Double errorRate = chatMessageRepository.getErrorRate(startDate, department);
        if (errorRate == null) {
            errorRate = 0.0;
        }

        // 최근 7일 질문 수
        Instant last7DaysStart = calculateStartDate(7);
        Long last7DaysQuestionCount = chatMessageRepository.countQuestionsByPeriod(
            last7DaysStart,
            department
        );

        // 활성 사용자 수 (최근 30일 기준)
        Instant last30DaysStart = calculateStartDate(30);
        Long activeUserCount = chatMessageRepository.countActiveUsers(
            last30DaysStart,
            department
        );

        // 응답 만족도 (%)
        Double satisfactionRate = chatFeedbackRepository.getSatisfactionRate(
            startDate,
            department
        );
        if (satisfactionRate == null) {
            satisfactionRate = 0.0;
        }

        // RAG 사용 비율 (%)
        Double ragUsageRate = chatMessageRepository.getRagUsageRate(startDate, department);
        if (ragUsageRate == null) {
            ragUsageRate = 0.0;
        }

        return new ChatDashboardResponse.DashboardSummaryResponse(
            todayQuestionCount,
            averageResponseTime,
            piiDetectionRate,
            errorRate,
            last7DaysQuestionCount,
            activeUserCount,
            satisfactionRate,
            ragUsageRate
        );
    }

    @Override
    public ChatDashboardResponse.RouteRatioResponse getRouteRatio(
        Integer periodDays,
        String department
    ) {
        if (periodDays == null) {
            periodDays = 30;
        }

        Instant startDate = calculateStartDate(periodDays);
        List<Object[]> results = chatMessageRepository.getRouteRatio(startDate, department);

        List<ChatDashboardResponse.RouteRatioItem> items = new ArrayList<>();
        for (Object[] row : results) {
            String routeType = (String) row[0];
            Double ratio = ((Number) row[2]).doubleValue();
            String routeName = ROUTE_NAME_MAP.getOrDefault(routeType, "기타/실험 라우트");

            items.add(new ChatDashboardResponse.RouteRatioItem(
                routeType,
                routeName,
                ratio
            ));
        }

        return new ChatDashboardResponse.RouteRatioResponse(items);
    }

    @Override
    public ChatDashboardResponse.TopKeywordsResponse getTopKeywords(
        Integer periodDays,
        String department
    ) {
        if (periodDays == null) {
            periodDays = 30;
        }

        Instant startDate = calculateStartDate(periodDays);
        List<Object[]> results = chatMessageRepository.getTopKeywords(
            startDate,
            department,
            5
        );

        List<ChatDashboardResponse.TopKeywordItem> items = new ArrayList<>();
        for (Object[] row : results) {
            String keyword = (String) row[0];
            Long questionCount = ((Number) row[1]).longValue();

            items.add(new ChatDashboardResponse.TopKeywordItem(
                keyword,
                questionCount
            ));
        }

        return new ChatDashboardResponse.TopKeywordsResponse(items);
    }

    @Override
    public ChatDashboardResponse.QuestionTrendResponse getQuestionTrend(
        Integer periodDays,
        String department
    ) {
        if (periodDays == null) {
            periodDays = 30;
        }

        Instant startDate = calculateStartDate(periodDays);
        List<Object[]> results = chatMessageRepository.getQuestionTrend(startDate, department);

        List<ChatDashboardResponse.QuestionTrendItem> items = new ArrayList<>();
        long totalQuestionCount = 0;
        double totalErrorRate = 0.0;
        int periodCount = 0;

        int weekNumber = 1;
        for (Object[] row : results) {
            String weekStart = (String) row[0];
            Long questionCount = ((Number) row[1]).longValue();
            Double errorRate = ((Number) row[2]).doubleValue();
            if (errorRate == null) {
                errorRate = 0.0;
            }

            totalQuestionCount += questionCount;
            totalErrorRate += errorRate;
            periodCount++;

            items.add(new ChatDashboardResponse.QuestionTrendItem(
                weekNumber + "주",
                questionCount,
                errorRate
            ));

            weekNumber++;
        }

        long averageQuestionCountPerPeriod = periodCount > 0
            ? totalQuestionCount / periodCount
            : 0L;
        double averageErrorRate = periodCount > 0
            ? totalErrorRate / periodCount
            : 0.0;

        return new ChatDashboardResponse.QuestionTrendResponse(
            totalQuestionCount,
            averageQuestionCountPerPeriod,
            averageErrorRate,
            items
        );
    }

    @Override
    public ChatDashboardResponse.DomainRatioResponse getDomainRatio(
        Integer periodDays,
        String department
    ) {
        if (periodDays == null) {
            periodDays = 30;
        }

        Instant startDate = calculateStartDate(periodDays);
        List<Object[]> results = chatMessageRepository.getDomainRatio(startDate, department);

        // 도메인별로 count 합산하기 위한 Map
        Map<String, Long> domainCountMap = new java.util.HashMap<>();
        long totalCount = 0;
        
        try {
            for (Object[] row : results) {
                if (row == null || row.length < 3) {
                    log.warn("Invalid row in domain ratio results: {}", java.util.Arrays.toString(row));
                    continue; // 잘못된 결과 행은 건너뛰기
                }
                
                try {
                    String domain = row[0] != null ? (String) row[0] : null;
                    // domain이 null인 경우 "OTHER"로 처리
                    if (domain == null || domain.isBlank()) {
                        domain = "OTHER";
                    }
                    
                    // 도메인 값 정규화 (대문자로 변환 및 공백 제거)
                    domain = domain.toUpperCase().trim();
                    
                    // SEC_POLICY 같은 값도 처리
                    if ("SEC_POLICY".equals(domain)) {
                        domain = "SECURITY";
                    }
                    
                    // count가 null인 경우 처리
                    Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    
                    // 같은 domain이면 count를 합산
                    domainCountMap.merge(domain, count, Long::sum);
                    totalCount += count;
                } catch (Exception e) {
                    log.error("Error processing domain ratio row: {}", java.util.Arrays.toString(row), e);
                    continue; // 에러가 발생한 행은 건너뛰기
                }
            }
        } catch (Exception e) {
            log.error("Error processing domain ratio results", e);
            // 에러가 발생해도 빈 리스트 반환
        }

        // Map을 List로 변환하고 count 기준으로 정렬, 전체 대비 비율 계산
        // 람다 표현식에서 사용하기 위해 final 변수로 복사
        final long finalTotalCount = totalCount;
        List<ChatDashboardResponse.DomainRatioItem> items = domainCountMap.entrySet().stream()
            .map(entry -> {
                String domain = entry.getKey();
                Long count = entry.getValue();
                // 전체 대비 비율 계산
                Double ratio = finalTotalCount > 0 ? (count * 100.0 / finalTotalCount) : 0.0;
                String domainName = DOMAIN_NAME_MAP.getOrDefault(domain, "기타");
                return new ChatDashboardResponse.DomainRatioItem(
                    domain,
                    domainName,
                    ratio
                );
            })
            .sorted((a, b) -> Double.compare(b.getRatio(), a.getRatio())) // ratio 기준 내림차순 정렬
            .collect(java.util.stream.Collectors.toList());

        return new ChatDashboardResponse.DomainRatioResponse(items);
    }

    /**
     * 기간(일수)에 따른 시작 날짜 계산
     * 
     * @param periodDays 기간 일수
     * @return 시작 시각
     */
    private Instant calculateStartDate(Integer periodDays) {
        LocalDate startDate = LocalDate.now().minusDays(periodDays);
        return startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}

