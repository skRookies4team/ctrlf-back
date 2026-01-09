package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.response.NotificationDtos;
import com.ctrlf.chat.strategy.StrategyEvent;
import com.ctrlf.chat.strategy.StrategyState;
import com.ctrlf.chat.telemetry.entity.TelemetryEvent;
import com.ctrlf.chat.telemetry.repository.TelemetryEventRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스
 * 
 * <p>TelemetryEvent와 StrategyEvent를 알림으로 변환하여 제공합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final TelemetryEventRepository telemetryEventRepository;

    /**
     * 최근 알림 목록 조회 (폴링용)
     * 
     * @param limit 최대 조회 개수
     * @return 최근 알림 목록
     */
    public List<NotificationDtos.NotificationResponse> getRecentNotifications(int limit) {
        List<NotificationDtos.NotificationResponse> notifications = new ArrayList<>();

        // StrategyEvent 기반 알림 (메모리 기반, DB 불필요) - 먼저 처리
        try {
            List<StrategyEvent> strategyEvents = StrategyState.getEvents();
            int strategySize = strategyEvents.size();
            List<StrategyEvent> recentStrategyEvents = strategyEvents.subList(
                Math.max(0, strategySize - 50), strategySize
            );

            for (StrategyEvent event : recentStrategyEvents) {
                NotificationDtos.NotificationResponse notification = convertStrategyEventToNotification(event);
                if (notification != null) {
                    notifications.add(notification);
                }
            }
        } catch (Exception e) {
            log.debug("StrategyEvent 기반 알림 변환 실패: {}", e.getMessage());
        }

        // TelemetryEvent 기반 알림 (DB 필요) - DB 에러가 있어도 StrategyEvent 알림은 반환
        try {
            Instant since = Instant.now().minusSeconds(3600); // 최근 1시간
            List<TelemetryEvent> securityEvents = telemetryEventRepository
                .findByEventTypeAndPeriodAndDept("SECURITY", since, Instant.now(), "all");

            for (TelemetryEvent event : securityEvents) {
                NotificationDtos.NotificationResponse notification = convertTelemetryEventToNotification(event);
                if (notification != null) {
                    notifications.add(notification);
                }
            }
        } catch (Exception e) {
            // DB 커넥션 풀 고갈 등으로 실패해도 StrategyEvent 알림은 반환
            log.debug("TelemetryEvent 기반 알림 조회 실패 (StrategyEvent 알림은 계속 반환): {}", e.getMessage());
        }

        // 타임스탬프 기준 내림차순 정렬 (최신순)
        notifications.sort(Comparator.comparing(
            NotificationDtos.NotificationResponse::getTimestamp
        ).reversed());

        // limit만큼만 반환
        return notifications.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * TelemetryEvent를 Notification으로 변환
     */
    private NotificationDtos.NotificationResponse convertTelemetryEventToNotification(
        TelemetryEvent event
    ) {
        try {
            String type = "info";
            String message = "보안 이벤트 발생";

            // payload에서 세부 정보 추출
            if (event.getPayload() != null && event.getPayload() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) event.getPayload();
                
                if (payload.containsKey("action")) {
                    String action = String.valueOf(payload.get("action"));
                    switch (action) {
                        case "PII_BLOCKED":
                            type = "warning";
                            message = "PII 감지 및 차단: " + payload.getOrDefault("reason", "");
                            break;
                        case "EXTERNAL_DOMAIN_BLOCKED":
                            type = "error";
                            message = "외부 도메인 접근 차단: " + payload.getOrDefault("domain", "");
                            break;
                        case "SUSPICIOUS_ACTIVITY":
                            type = "error";
                            message = "의심스러운 활동 감지: " + payload.getOrDefault("description", "");
                            break;
                        default:
                            message = "보안 이벤트: " + action;
                    }
                }
            }

            return new NotificationDtos.NotificationResponse(
                event.getEventId().toString(),
                event.getOccurredAt().toString(),
                type,
                message,
                null // 폴링 응답에서는 읽음 여부를 null로 설정
            );
        } catch (Exception e) {
            log.warn("TelemetryEvent를 알림으로 변환 실패: eventId={}, error={}",
                event.getEventId(), e.getMessage());
            return null;
        }
    }

    /**
     * StrategyEvent를 Notification으로 변환
     */
    private NotificationDtos.NotificationResponse convertStrategyEventToNotification(
        StrategyEvent event
    ) {
        try {
            String type = "info";
            String message = String.format(
                "도메인 '%s'의 전략이 변경되었습니다: %s → %s (%s)",
                event.getDomain(),
                event.getFromStrategy(),
                event.getToStrategy(),
                event.getReason()
            );

            // 전략 변경이 성능 관련이면 warning
            if (event.getReason().contains("LATENCY") || event.getReason().contains("ERROR")) {
                type = "warning";
            }

            LocalDateTime occurredAt = event.getOccurredAt();
            Instant instant = occurredAt.atZone(ZoneOffset.systemDefault()).toInstant();

            return new NotificationDtos.NotificationResponse(
                "strategy-" + event.getDomain() + "-" + occurredAt.toString(),
                instant.toString(),
                type,
                message,
                null
            );
        } catch (Exception e) {
            log.warn("StrategyEvent를 알림으로 변환 실패: domain={}, error={}",
                event.getDomain(), e.getMessage());
            return null;
        }
    }
}

