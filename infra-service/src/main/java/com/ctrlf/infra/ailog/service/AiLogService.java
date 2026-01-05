package com.ctrlf.infra.ailog.service;

import com.ctrlf.infra.ailog.dto.AiLogDtos;
import com.ctrlf.infra.ailog.entity.AiLog;
import com.ctrlf.infra.ailog.repository.AiLogRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 로그 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiLogService {

    private final AiLogRepository aiLogRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * AI 로그 Bulk 저장
     */
    public AiLogDtos.BulkResponse saveBulkLogs(AiLogDtos.BulkRequest request) {
        int received = request.getLogs().size();
        int saved = 0;
        int failed = 0;
        List<AiLogDtos.ErrorItem> errors = new ArrayList<>();

        for (int i = 0; i < request.getLogs().size(); i++) {
            AiLogDtos.LogItem logItem = request.getLogs().get(i);
            try {
                AiLog aiLog = new AiLog();
                aiLog.setCreatedAt(logItem.getCreatedAt());
                aiLog.setUserId(logItem.getUserId());
                aiLog.setUserRole(logItem.getUserRole());
                aiLog.setDepartment(logItem.getDepartment());
                aiLog.setDomain(logItem.getDomain());
                aiLog.setRoute(logItem.getRoute());
                aiLog.setModelName(logItem.getModelName());
                aiLog.setHasPiiInput(logItem.getHasPiiInput());
                aiLog.setHasPiiOutput(logItem.getHasPiiOutput());
                aiLog.setRagUsed(logItem.getRagUsed());
                aiLog.setRagSourceCount(logItem.getRagSourceCount());
                aiLog.setLatencyMsTotal(logItem.getLatencyMsTotal());
                aiLog.setErrorCode(logItem.getErrorCode());
                aiLog.setTraceId(logItem.getTraceId());
                aiLog.setConversationId(logItem.getConversationId());
                aiLog.setTurnId(logItem.getTurnId());
                aiLog.setReceivedAt(Instant.now());

                aiLogRepository.save(aiLog);
                saved++;

            } catch (Exception e) {
                failed++;
                errors.add(new AiLogDtos.ErrorItem(
                    i,
                    "SAVE_ERROR",
                    e.getMessage()
                ));
                log.warn("AI 로그 저장 실패: index={}, error={}", i, e.getMessage());
            }
        }

        log.info("AI 로그 bulk 저장 완료: received={}, saved={}, failed={}", 
            received, saved, failed);

        return new AiLogDtos.BulkResponse(received, saved, failed, errors);
    }

    /**
     * 관리자 대시보드 로그 목록 조회
     */
    @Transactional(readOnly = true)
    public AiLogDtos.PageResponse<AiLogDtos.LogListItem> getLogs(
        String period,
        String department,
        String domain,
        String route,
        String model,
        Boolean onlyError,
        Boolean hasPiiOnly,
        Integer page,
        Integer size
    ) {
        // 기간 계산
        Instant[] periodRange = calculatePeriodRange(period);
        Instant startDate = periodRange[0];
        Instant endDate = periodRange[1];

        // 페이징 설정
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? Math.min(size, 100) : 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // 조회
        Page<AiLog> logPage = aiLogRepository.findLogsWithFilters(
            startDate,
            endDate,
            department,
            domain,
            route,
            model,
            onlyError,
            hasPiiOnly,
            pageable
        );

        // DTO 변환
        List<AiLogDtos.LogListItem> content = logPage.getContent().stream()
            .map(this::convertToLogListItem)
            .toList();

        return new AiLogDtos.PageResponse<>(
            content,
            logPage.getTotalElements(),
            logPage.getTotalPages(),
            logPage.getNumber(),
            logPage.getSize()
        );
    }

    /**
     * 엔티티를 DTO로 변환
     */
    private AiLogDtos.LogListItem convertToLogListItem(AiLog aiLog) {
        String createdAtStr = aiLog.getCreatedAt() != null
            ? aiLog.getCreatedAt().atZone(ZoneId.systemDefault())
                .format(DATE_TIME_FORMATTER)
            : null;

        return new AiLogDtos.LogListItem(
            aiLog.getId(),
            createdAtStr,
            aiLog.getUserId(),
            aiLog.getUserRole(),
            aiLog.getDepartment(),
            aiLog.getDomain(),
            aiLog.getRoute(),
            aiLog.getModelName(),
            aiLog.getHasPiiInput(),
            aiLog.getHasPiiOutput(),
            aiLog.getRagUsed(),
            aiLog.getRagSourceCount(),
            aiLog.getLatencyMsTotal(),
            aiLog.getErrorCode()
        );
    }

    /**
     * 기간 계산
     */
    private Instant[] calculatePeriodRange(String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        if (period == null || period.isBlank()) {
            period = "30";
        }

        switch (period) {
            case "7":
                startDate = endDate.minusDays(7);
                break;
            case "30":
                startDate = endDate.minusDays(30);
                break;
            case "90":
                startDate = endDate.minusDays(90);
                break;
            default:
                startDate = endDate.minusDays(30);
        }

        return new Instant[] {
            startDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        };
    }
}

