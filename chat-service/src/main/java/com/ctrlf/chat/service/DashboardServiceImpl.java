package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.response.DashboardResponseDtos;
import com.ctrlf.chat.telemetry.enums.Bucket;
import com.ctrlf.chat.telemetry.enums.Period;
import com.ctrlf.chat.telemetry.repository.TelemetryEventRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 대시보드 서비스 구현체 (새 스펙)
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TelemetryEventRepository telemetryEventRepository;

    private static final Map<String, String> DOMAIN_LABEL_MAP = Map.of(
        "POLICY", "규정 안내",
        "FAQ", "FAQ",
        "EDUCATION", "교육",
        "QUIZ", "퀴즈",
        "ETC", "기타"
    );

    @Override
    public DashboardResponseDtos.ChatSummaryResponse getChatSummary(
        String period,
        String dept,
        boolean refresh
    ) {
        Period periodEnum = Period.fromString(period);
        String deptId = normalizeDept(dept);

        Instant[] dateRange = calculateDateRange(periodEnum);
        Instant startDate = dateRange[0];
        Instant endDate = dateRange[1];

        // 오늘 질문 수
        Long todayQuestionCount = telemetryEventRepository.countTodayChatTurns(deptId);

        // 기간 내 질문 수
        Long periodQuestionCount = telemetryEventRepository.countChatTurns(startDate, endDate, deptId);

        // 기간 내 일평균 질문 수
        int days = periodEnum.toDays();
        Long periodDailyAvgQuestionCount = days > 0 ? periodQuestionCount / days : 0L;

        // 활성 사용자 수
        Long activeUsers = telemetryEventRepository.countActiveUsers(startDate, endDate, deptId);

        // 평균 지연 시간
        Double avgLatencyMs = telemetryEventRepository.getAverageLatencyMs(startDate, endDate, deptId);
        if (avgLatencyMs == null) {
            avgLatencyMs = 0.0;
        }

        // PII 감지 비율
        Double piiDetectRate = telemetryEventRepository.getPiiDetectRate(startDate, endDate, deptId);
        if (piiDetectRate == null) {
            piiDetectRate = 0.0;
        }

        // 에러율
        Double errorRate = telemetryEventRepository.getErrorRate(startDate, endDate, deptId);
        if (errorRate == null) {
            errorRate = 0.0;
        }

        // 만족도/불만족도
        Object[] feedbackCounts = telemetryEventRepository.getFeedbackCounts(startDate, endDate, deptId);
        Double satisfactionRate = null;
        Double dislikeRate = null;
        if (feedbackCounts != null && feedbackCounts.length >= 2) {
            Number likeCount = (Number) feedbackCounts[0];
            Number dislikeCount = (Number) feedbackCounts[1];
            double total = likeCount.doubleValue() + dislikeCount.doubleValue();
            if (total > 0) {
                satisfactionRate = (likeCount.doubleValue() / total) * 100.0;
                dislikeRate = (dislikeCount.doubleValue() / total) * 100.0;
            }
        }

        // RAG 사용 비율
        Double ragUsageRate = telemetryEventRepository.getRagUsageRate(startDate, endDate, deptId);
        if (ragUsageRate == null) {
            ragUsageRate = 0.0;
        }

        return new DashboardResponseDtos.ChatSummaryResponse(
            periodEnum.getValue(),
            dept,
            todayQuestionCount,
            periodQuestionCount,
            periodDailyAvgQuestionCount,
            activeUsers,
            avgLatencyMs,
            piiDetectRate,
            errorRate,
            satisfactionRate,
            dislikeRate,
            ragUsageRate
        );
    }

    @Override
    public DashboardResponseDtos.ChatTrendsResponse getChatTrends(
        String period,
        String dept,
        String bucket,
        boolean refresh
    ) {
        Period periodEnum = Period.fromString(period);
        Bucket bucketEnum = Bucket.fromString(bucket);
        String deptId = normalizeDept(dept);

        Instant[] dateRange = calculateDateRange(periodEnum);
        Instant startDate = dateRange[0];
        Instant endDate = dateRange[1];

        List<Object[]> results = telemetryEventRepository.getTrends(
            startDate,
            endDate,
            deptId,
            bucketEnum.getValue()
        );

        List<DashboardResponseDtos.TrendItem> series = new ArrayList<>();
        for (Object[] row : results) {
            String bucketStart = (String) row[0];
            Long questionCount = ((Number) row[1]).longValue();
            Double errorRate = ((Number) row[2]).doubleValue();
            if (errorRate == null) {
                errorRate = 0.0;
            }

            series.add(new DashboardResponseDtos.TrendItem(
                bucketStart,
                questionCount,
                errorRate
            ));
        }

        return new DashboardResponseDtos.ChatTrendsResponse(
            bucketEnum.getValue(),
            series
        );
    }

    @Override
    public DashboardResponseDtos.DomainShareResponse getDomainShare(
        String period,
        String dept,
        boolean refresh
    ) {
        Period periodEnum = Period.fromString(period);
        String deptId = normalizeDept(dept);

        Instant[] dateRange = calculateDateRange(periodEnum);
        Instant startDate = dateRange[0];
        Instant endDate = dateRange[1];

        List<Object[]> results = telemetryEventRepository.getDomainShare(startDate, endDate, deptId);

        List<DashboardResponseDtos.DomainShareItem> items = new ArrayList<>();
        for (Object[] row : results) {
            String domain = (String) row[0];
            if (domain == null) {
                domain = "ETC";
            }
            Long questionCount = ((Number) row[1]).longValue();
            Double share = ((Number) row[2]).doubleValue();
            if (share == null) {
                share = 0.0;
            }

            String label = DOMAIN_LABEL_MAP.getOrDefault(domain, "기타");

            items.add(new DashboardResponseDtos.DomainShareItem(
                domain,
                label,
                questionCount,
                share
            ));
        }

        return new DashboardResponseDtos.DomainShareResponse(items);
    }

    @Override
    public DashboardResponseDtos.SecurityMetricsResponse getSecurityMetrics(
        String period,
        String dept,
        boolean refresh
    ) {
        Period periodEnum = Period.fromString(period);
        String deptId = normalizeDept(dept);

        Instant[] dateRange = calculateDateRange(periodEnum);
        Instant startDate = dateRange[0];
        Instant endDate = dateRange[1];

        // 보안 이벤트 수
        Object[] securityCounts = telemetryEventRepository.getSecurityCounts(startDate, endDate, deptId);
        Long piiBlockCount = 0L;
        Long externalDomainBlockCount = 0L;
        if (securityCounts != null && securityCounts.length >= 2) {
            piiBlockCount = ((Number) securityCounts[0]).longValue();
            externalDomainBlockCount = ((Number) securityCounts[1]).longValue();
        }

        // PII 추이
        List<Object[]> piiTrendResults = telemetryEventRepository.getPiiTrend(startDate, endDate, deptId);
        List<DashboardResponseDtos.PiiTrendItem> piiTrend = new ArrayList<>();
        for (Object[] row : piiTrendResults) {
            String bucketStart = (String) row[0];
            Double inputDetectRate = ((Number) row[1]).doubleValue();
            Double outputDetectRate = ((Number) row[2]).doubleValue();
            if (inputDetectRate == null) {
                inputDetectRate = 0.0;
            }
            if (outputDetectRate == null) {
                outputDetectRate = 0.0;
            }

            piiTrend.add(new DashboardResponseDtos.PiiTrendItem(
                bucketStart,
                inputDetectRate,
                outputDetectRate
            ));
        }

        return new DashboardResponseDtos.SecurityMetricsResponse(
            piiBlockCount,
            externalDomainBlockCount,
            piiTrend
        );
    }

    @Override
    public DashboardResponseDtos.PerformanceMetricsResponse getPerformanceMetrics(
        String period,
        String dept,
        boolean refresh
    ) {
        Period periodEnum = Period.fromString(period);
        String deptId = normalizeDept(dept);

        Instant[] dateRange = calculateDateRange(periodEnum);
        Instant startDate = dateRange[0];
        Instant endDate = dateRange[1];

        // 불만족 비율
        Object[] dislikeRateResult = telemetryEventRepository.getDislikeRate(startDate, endDate, deptId);
        Double dislikeRate = null;
        if (dislikeRateResult != null && dislikeRateResult.length >= 2) {
            Number dislikeCount = (Number) dislikeRateResult[0];
            Number likeCount = (Number) dislikeRateResult[1];
            double total = dislikeCount.doubleValue() + likeCount.doubleValue();
            if (total > 0) {
                dislikeRate = (dislikeCount.doubleValue() / total) * 100.0;
            }
        }

        // 재질문 비율 (MVP: 동일 conversation, 최근 3턴 내, 동일 intentMain 반복)
        // TODO: MVP 구현 - 현재는 0으로 반환
        Double repeatRate = 0.0;
        String repeatDefinition = "MVP: same conversation, within last 3 turns, same intentMain repeated";

        // OOS 수
        Long oosCount = telemetryEventRepository.getOosCount(startDate, endDate, deptId);
        if (oosCount == null) {
            oosCount = 0L;
        }

        // 지연 시간 히스토그램
        List<Object[]> latencyHistogramResults = telemetryEventRepository.getLatencyHistogram(
            startDate,
            endDate,
            deptId
        );
        List<DashboardResponseDtos.LatencyHistogramItem> latencyHistogram = new ArrayList<>();
        for (Object[] row : latencyHistogramResults) {
            String range = (String) row[0];
            Long count = ((Number) row[1]).longValue();

            latencyHistogram.add(new DashboardResponseDtos.LatencyHistogramItem(range, count));
        }

        // 모델별 지연 시간
        List<Object[]> modelLatencyResults = telemetryEventRepository.getModelLatency(
            startDate,
            endDate,
            deptId
        );
        List<DashboardResponseDtos.ModelLatencyItem> modelLatency = new ArrayList<>();
        for (Object[] row : modelLatencyResults) {
            String model = (String) row[0];
            Double avgLatencyMs = ((Number) row[1]).doubleValue();
            if (avgLatencyMs == null) {
                avgLatencyMs = 0.0;
            }

            modelLatency.add(new DashboardResponseDtos.ModelLatencyItem(model, avgLatencyMs));
        }

        return new DashboardResponseDtos.PerformanceMetricsResponse(
            dislikeRate,
            repeatRate,
            repeatDefinition,
            oosCount,
            latencyHistogram,
            modelLatency
        );
    }

    /**
     * Period를 날짜 범위로 변환
     */
    private Instant[] calculateDateRange(Period period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today.plusDays(1); // 포함하기 위해 +1일

        if (period == Period.TODAY) {
            startDate = today;
        } else {
            int days = period.toDays();
            startDate = today.minusDays(days);
        }

        return new Instant[] {
            startDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        };
    }

    /**
     * 부서 필터 정규화
     */
    private String normalizeDept(String dept) {
        if (dept == null || dept.isBlank() || "all".equalsIgnoreCase(dept)) {
            return "all";
        }
        return dept;
    }
}

