package com.ctrlf.infra.config;

import com.ctrlf.infra.telemetry.entity.TelemetryEvent;
import com.ctrlf.infra.telemetry.repository.TelemetryEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 텔레메트리 이벤트 로컬 시드 (AI 로그 더미 데이터).
 * 활성화: --spring.profiles.active=local,local-seed
 */
@Profile("local-seed")
@Order(2)
@Component
public class TelemetrySeedRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(TelemetrySeedRunner.class);

    private final TelemetryEventRepository telemetryEventRepository;
    private final ObjectMapper objectMapper;

    public TelemetrySeedRunner(
        TelemetryEventRepository telemetryEventRepository,
        ObjectMapper objectMapper
    ) {
        this.telemetryEventRepository = telemetryEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedTelemetryEvents();
    }

    private void seedTelemetryEvents() {
        log.info("Starting telemetry event seed data generation...");

        // FAQ 후보가 되기 위한 조건:
        // - domain == "POLICY"
        // - intent == "POLICY_QA"
        // - question_masked 존재
        // - 동일한 질문이 최소 3개 이상 (MIN_QUESTION_COUNT = 3)

        // 질문 1: 연차 신청 관련 (5개 반복)
        String question1 = "연차 신청은 어떻게 하나요?";
        createTelemetryEvents(question1, 5, "RAG");

        // 질문 2: 휴가 정책 관련 (4개 반복)
        String question2 = "연차 사용 규정은 어떻게 되나요?";
        createTelemetryEvents(question2, 4, "RAG");

        // 질문 3: 출근 시간 관련 (3개 반복)
        String question3 = "출근 시간은 언제인가요?";
        createTelemetryEvents(question3, 3, "FAQ");

        // 질문 4: 복지 혜택 관련 (6개 반복)
        String question4 = "사내 복지 혜택은 무엇이 있나요?";
        createTelemetryEvents(question4, 6, "RAG");

        // 질문 5: 퇴근 시간 관련 (3개 반복)
        String question5 = "퇴근 시간은 언제인가요?";
        createTelemetryEvents(question5, 3, "FAQ");

        // 다른 도메인/인텐트 조합도 추가 (필터링되지 않아야 함)
        String questionOther = "보안 정책은 어떻게 되나요?";
        createTelemetryEvents(questionOther, 2, "RAG", "SECURITY", "SECURITY_QA");

        log.info("Telemetry event seed data generation completed!");
    }

    /**
     * 텔레메트리 이벤트 생성 (POLICY 도메인, POLICY_QA 인텐트)
     */
    private void createTelemetryEvents(
        String questionMasked,
        int count,
        String route
    ) {
        createTelemetryEvents(questionMasked, count, route, "POLICY", "POLICY_QA");
    }

    /**
     * 텔레메트리 이벤트 생성
     */
    private void createTelemetryEvents(
        String questionMasked,
        int count,
        String route,
        String domain,
        String intent
    ) {
        Instant baseTime = Instant.now().minusSeconds(86400); // 1일 전부터 시작
        String conversationId = UUID.randomUUID().toString();

        for (int i = 0; i < count; i++) {
            TelemetryEvent event = new TelemetryEvent();
            event.setEventId(UUID.randomUUID());
            event.setSource("ai-gateway");
            event.setSentAt(baseTime.plusSeconds(i * 3600)); // 1시간 간격
            event.setEventType("CHAT_TURN");
            event.setTraceId(UUID.randomUUID());
            event.setConversationId(conversationId);
            event.setTurnId(i + 1);
            event.setUserId("00000000-0000-0000-0000-000000000000");
            event.setDeptId("총무팀");
            event.setOccurredAt(baseTime.plusSeconds(i * 3600));
            event.setReceivedAt(Instant.now().minusSeconds(count - i));

            // Payload 생성
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("domain", domain);
            payloadMap.put("intent", intent);
            payloadMap.put("route", route);
            payloadMap.put("question_masked", questionMasked);
            payloadMap.put("model", "gpt-4o-mini");
            payloadMap.put("latencyMsTotal", 450 + i * 10);
            payloadMap.put("latencyMsLlm", 300 + i * 5);
            payloadMap.put("piiDetectedInput", false);
            payloadMap.put("piiDetectedOutput", false);
            payloadMap.put("oos", false);

            // Map을 JSON 문자열로 변환하여 저장
            // Hibernate의 @JdbcTypeCode(SqlTypes.JSON)가 Map을 직접 처리하지 못하는 경우를 대비
            try {
                String payloadJson = objectMapper.writeValueAsString(payloadMap);
                event.setPayload(payloadJson);
            } catch (Exception e) {
                log.error("Failed to serialize payload to JSON: {}", e.getMessage(), e);
                // 실패 시 Map을 그대로 저장 (Hibernate가 처리할 수 있는 경우)
                event.setPayload(payloadMap);
            }

            telemetryEventRepository.save(event);
        }

        log.info(
            "Seed created: {} telemetry events | domain={}, intent={}, question='{}'",
            count, domain, intent, questionMasked
        );
    }
}

