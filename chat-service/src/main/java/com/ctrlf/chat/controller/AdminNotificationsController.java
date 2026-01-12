// === path : AdminNotificationsController.java
package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.response.NotificationDtos;
import com.ctrlf.chat.service.NotificationService;
import com.ctrlf.chat.strategy.StrategyState;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 관리자 알림 API 컨트롤러
 *
 * <p>관리자 대시보드에서 실시간 알림을 수신하는 API를 제공합니다.</p>
 *
 * @author CtrlF Team
 * @since 1.0.0
 */
@Tag(name = "Admin-Notifications", description = "관리자 알림 API (ADMIN)")
@RestController
@RequestMapping("/admin/notifications")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationsController {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /** SSE 연결 관리 리스트 */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * SSE 스트림 구독
     *
     * <p>Server-Sent Events를 통해 실시간 알림을 수신합니다.</p>
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "알림 스트림 구독 (SSE)",
        description = "Server-Sent Events를 통해 실시간 알림을 수신합니다. 연결이 유지되는 동안 새 알림을 자동으로 전송합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "SSE 스트림 시작",
            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public SseEmitter subscribeToNotifications() {
        SseEmitter emitter = new SseEmitter(3600000L); // 1시간 타임아웃

        // 초기 연결 알림
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("{\"message\":\"알림 스트림에 연결되었습니다.\"}"));
        } catch (IOException e) {
            log.warn("SSE 초기 연결 알림 전송 실패", e);
            emitter.completeWithError(e);
            return emitter;
        }

        // 에러 및 완료 핸들러
        emitter.onCompletion(() -> {
            log.debug("SSE 연결 완료 (클라이언트 종료)");
            removeEmitter(emitter);
        });

        emitter.onError((ex) -> {
            log.debug("SSE 연결 오류: {}", ex.getMessage());
            removeEmitter(emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE 연결 타임아웃");
            removeEmitter(emitter);
        });

        emitters.add(emitter);
        log.info("SSE 연결 추가: 현재 연결 수 = {}", emitters.size());

        // 초기 알림 전송 (최근 10개) - 비동기로 처리하여 DB 에러가 있어도 SSE 연결은 유지
        try {
            // 비동기로 실행하여 DB 커넥션 문제가 있어도 SSE 연결은 계속 유지
            CompletableFuture.runAsync(() -> {
                try {
                    List<NotificationDtos.NotificationResponse> recent = notificationService.getRecentNotifications(10);
                    log.info("초기 알림 조회 완료: {}개", recent.size());
                    for (NotificationDtos.NotificationResponse notification : recent) {
                        try {
                            // ObjectMapper를 사용하여 명시적으로 JSON 직렬화
                            String jsonData = objectMapper.writeValueAsString(notification);
                            safeSend(emitter, SseEmitter.event()
                                .name("notification")
                                .data(jsonData));
                            log.debug("알림 전송: id={}, message={}", notification.getId(), notification.getMessage());
                        } catch (Exception e) {
                            log.warn("알림 직렬화/전송 실패: {}", e.getMessage());
                        }
                    }
                    if (!recent.isEmpty()) {
                        log.info("초기 알림 {}개 전송 완료", recent.size());
                    } else {
                        log.info("초기 알림 없음 (StrategyEvent 또는 TelemetryEvent 없음)");
                    }
                } catch (Exception e) {
                    // DB 커넥션 풀 고갈 등으로 실패해도 SSE 연결은 유지
                    log.debug("초기 알림 전송 실패 (SSE 연결은 유지): {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            log.debug("초기 알림 전송 스케줄링 실패 (SSE 연결은 유지): {}", e.getMessage());
        }

        return emitter;
    }

    /**
     * 최근 알림 목록 조회 (폴링용)
     */
    @GetMapping("/recent")
    @Operation(
        summary = "최근 알림 목록 조회",
        description = "폴링 방식으로 최근 알림 목록을 조회합니다. limit 파라미터로 조회 개수를 제한할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = NotificationDtos.RecentNotificationsResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<NotificationDtos.RecentNotificationsResponse> getRecentNotifications(
        @Parameter(description = "최대 조회 개수", example = "50")
        @RequestParam(value = "limit", required = false, defaultValue = "50") int limit
    ) {
        List<NotificationDtos.NotificationResponse> notifications =
            notificationService.getRecentNotifications(limit);

        return ResponseEntity.ok(
            new NotificationDtos.RecentNotificationsResponse(notifications)
        );
    }

    /**
     * 주기적으로 새 알림을 확인하고 SSE 클라이언트에게 전송
     *
     * <p>30초마다 실행되며, 최근 알림을 전송합니다.</p>
     */
    @Scheduled(fixedRate = 30000) // 30초마다
    public void checkAndBroadcastNewNotifications() {
        if (emitters.isEmpty()) {
            return;
        }

        try {
            // 최근 알림 조회
            List<NotificationDtos.NotificationResponse> newNotifications =
                notificationService.getRecentNotifications(20);

            if (newNotifications.isEmpty()) {
                return;
            }

            // 각 SSE 클라이언트에게 알림 전송
            for (SseEmitter emitter : emitters) {
                for (NotificationDtos.NotificationResponse notification : newNotifications) {
                    try {
                        // ObjectMapper를 사용하여 명시적으로 JSON 직렬화
                        String jsonData = objectMapper.writeValueAsString(notification);
                        safeSend(emitter, SseEmitter.event()
                            .name("notification")
                            .data(jsonData));
                    } catch (Exception e) {
                        log.warn("알림 직렬화/전송 실패: {}", e.getMessage());
                    }
                }
            }

            log.debug("알림 브로드캐스트 완료: {}개 알림을 {}개 연결에 전송",
                newNotifications.size(), emitters.size());
        } catch (Exception e) {
            // DB 커넥션 풀 고갈 등으로 실패해도 다음 주기에서 재시도
            log.debug("알림 브로드캐스트 실패 (다음 주기에서 재시도): {}", e.getMessage());
        }
    }

    /**
     * 테스트용 알림 생성 (개발/테스트 전용)
     *
     * <p>StrategyEvent를 생성하여 테스트 알림을 만듭니다.</p>
     */
    @PostMapping("/test")
    @Operation(
        summary = "테스트 알림 생성",
        description = "테스트용 알림을 생성합니다. StrategyEvent를 생성하여 즉시 알림이 생성됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "테스트 알림 생성 성공",
            content = @Content(schema = @Schema(implementation = NotificationDtos.NotificationResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<NotificationDtos.NotificationResponse> createTestNotification(
        @Parameter(description = "알림 타입", example = "info")
        @RequestParam(value = "type", required = false, defaultValue = "info") String type,
        @Parameter(description = "알림 메시지", example = "테스트 알림입니다")
        @RequestParam(value = "message", required = false, defaultValue = "테스트 알림입니다") String message,
        @Parameter(description = "도메인", example = "TEST")
        @RequestParam(value = "domain", required = false, defaultValue = "TEST") String domain
    ) {
        log.info("테스트 알림 생성 요청: type={}, message={}, domain={}", type, message, domain);

        // StrategyEvent 생성 (StrategyState.recordEvent를 통해 이벤트 기록)
        Map<String, Object> oldStrategy = new HashMap<>();
        oldStrategy.put("reason", "OLD_REASON");

        Map<String, Object> newStrategy = new HashMap<>();
        // 메시지를 reason으로 사용
        String reason = type.equals("warning") ? "LATENCY_WARNING" :
                       type.equals("error") ? "ERROR_OCCURRED" : message;
        newStrategy.put("reason", reason);

        // StrategyState에 이벤트 추가
        try {
            StrategyState.recordEvent(domain, oldStrategy, newStrategy);
            log.info("StrategyEvent 생성 완료: domain={}, reason={}", domain, reason);
        } catch (Exception e) {
            log.warn("StrategyEvent 생성 실패: {}", e.getMessage(), e);
        }

        // NotificationService를 통해 알림 변환
        NotificationDtos.NotificationResponse notification =
            new NotificationDtos.NotificationResponse(
                "test-" + System.currentTimeMillis(),
                LocalDateTime.now().toString(),
                type,
                message,
                null
            );

        // SSE 연결된 클라이언트에게 즉시 전송
        try {
            String jsonData = objectMapper.writeValueAsString(notification);
            for (SseEmitter emitter : emitters) {
                safeSend(emitter, SseEmitter.event()
                    .name("notification")
                    .data(jsonData));
            }
            log.info("테스트 알림 전송 완료: {}개 연결에 전송", emitters.size());
        } catch (Exception e) {
            log.warn("테스트 알림 SSE 전송 실패: {}", e.getMessage());
        }

        return ResponseEntity.ok(notification);
    }

    /**
     * 시연용 더미 알림 시드(3종) 생성 (데모/시연 영상용)
     *
     * <p>Normal → Warning → Critical 더미 알림을 SSE로 즉시 전송합니다.</p>
     */
    @PostMapping("/demo/seed")
    @Operation(
        summary = "시연용 더미 알림 시드(3종) 생성",
        description = "Normal → Warning → Critical 더미 알림을 SSE로 즉시 전송합니다. (데모/시연 영상용)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> seedDemoNotifications(
        @Parameter(description = "도메인", example = "AI")
        @RequestParam(value = "domain", required = false, defaultValue = "AI") String domain
    ) {
        NotificationDtos.NotificationResponse n1 =
            new NotificationDtos.NotificationResponse(
                "demo-normal-" + System.currentTimeMillis(),
                LocalDateTime.now().toString(),
                "info",
                "🟢 AI System: Normal\nLLM: EXAONE\nRAG: Milvus\nRPS: 43 / 50",
                null
            );

        NotificationDtos.NotificationResponse n2 =
            new NotificationDtos.NotificationResponse(
                "demo-warning-" + (System.currentTimeMillis() + 1),
                LocalDateTime.now().toString(),
                "warning",
                "🟡 AI System: Degraded\nLatency high\nRetrying RAG…",
                null
            );

        NotificationDtos.NotificationResponse n3 =
            new NotificationDtos.NotificationResponse(
                "demo-critical-" + (System.currentTimeMillis() + 2),
                LocalDateTime.now().toString(),
                "error",
                "🔴 AI System: Safe Mode\n\nReason: Infrastructure failure\nAction Taken:\n✔ RAG switched to RAGFlow\n✔ LLM switched to OpenAI\n✔ Traffic throttled\n\nUser experience protected.",
                null
            );

        broadcastNotification(n1);
        broadcastNotification(n2);
        broadcastNotification(n3);

        Map<String, Object> result = new HashMap<>();
        result.put("domain", domain);
        result.put("seeded", 3);
        result.put("sentToEmitters", emitters.size());
        return ResponseEntity.ok(result);
    }

    /**
     * NotificationResponse를 모든 SSE 연결에 브로드캐스트합니다.
     */
    private void broadcastNotification(NotificationDtos.NotificationResponse notification) {
        try {
            String jsonData = objectMapper.writeValueAsString(notification);
            for (SseEmitter emitter : emitters) {
                safeSend(emitter, SseEmitter.event()
                    .name("notification")
                    .data(jsonData));
            }
        } catch (Exception e) {
            log.warn("Demo notification broadcast failed: {}", e.getMessage());
        }
    }

    /**
     * SseEmitter에 안전하게 데이터를 전송합니다.
     */
    private void safeSend(SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IllegalStateException e) {
            // 이미 완료된 emitter에 대한 send 시도 (정상적인 상황)
            log.debug("SSE emitter already completed, removing from list");
            removeEmitter(emitter);
        } catch (IOException e) {
            // 클라이언트 연결이 이미 종료된 경우 (정상적인 상황)
            // 스택 트레이스를 출력하지 않고 간단히 로그만 남김
            log.debug("SSE connection closed by client: {}", e.getMessage());
            removeEmitter(emitter);
        } catch (Exception e) {
            // 예상치 못한 오류만 WARN 레벨로 로깅
            log.warn("Unexpected error while sending SSE event: {}", e.getMessage());
            removeEmitter(emitter);
        }
    }

    /**
     * Emitter를 리스트에서 안전하게 제거합니다 (중복 제거 방지).
     */
    private void removeEmitter(SseEmitter emitter) {
        if (emitters.remove(emitter)) {
            log.debug("SSE emitter removed from list: 현재 연결 수 = {}", emitters.size());
        }
    }
}
