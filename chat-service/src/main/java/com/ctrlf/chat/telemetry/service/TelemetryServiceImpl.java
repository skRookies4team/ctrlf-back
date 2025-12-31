package com.ctrlf.chat.telemetry.service;

import com.ctrlf.chat.telemetry.dto.TelemetryEventRequestDtos;
import com.ctrlf.chat.telemetry.dto.TelemetryEventResponseDtos;
import com.ctrlf.chat.telemetry.entity.TelemetryEvent;
import com.ctrlf.chat.telemetry.repository.TelemetryEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Telemetry 이벤트 수집 서비스 구현체
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

    private final TelemetryEventRepository telemetryEventRepository;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    @Transactional
    public TelemetryEventResponseDtos.EventsResponse collectEvents(
        TelemetryEventRequestDtos.EventsRequest request
    ) {
        int received = request.getEvents() != null ? request.getEvents().size() : 0;
        int accepted = 0;
        int rejected = 0;
        List<TelemetryEventResponseDtos.EventError> errors = new ArrayList<>();

        if (request.getEvents() == null || request.getEvents().isEmpty()) {
            return new TelemetryEventResponseDtos.EventsResponse(received, accepted, rejected, errors);
        }

        for (TelemetryEventRequestDtos.Event eventDto : request.getEvents()) {
            try {
                // Idempotency 체크
                if (eventDto.getEventId() != null) {
                    boolean exists = telemetryEventRepository.findByEventId(eventDto.getEventId())
                        .isPresent();
                    if (exists) {
                        // 이미 존재하는 이벤트는 수락으로 처리 (idempotent)
                        accepted++;
                        continue;
                    }
                }

                // 필수 필드 검증
                String validationError = validateEvent(eventDto);
                if (validationError != null) {
                    rejected++;
                    errors.add(new TelemetryEventResponseDtos.EventError(
                        eventDto.getEventId() != null ? eventDto.getEventId().toString() : "unknown",
                        "INVALID_FIELD",
                        validationError
                    ));
                    continue;
                }

                // 엔티티 생성 및 저장
                TelemetryEvent event = toEntity(request.getSource(), request.getSentAt(), eventDto);
                telemetryEventRepository.save(event);
                accepted++;

            } catch (Exception e) {
                log.error("이벤트 저장 실패: eventId={}", eventDto.getEventId(), e);
                rejected++;
                errors.add(new TelemetryEventResponseDtos.EventError(
                    eventDto.getEventId() != null ? eventDto.getEventId().toString() : "unknown",
                    "INTERNAL_ERROR",
                    "이벤트 저장 중 오류 발생: " + e.getMessage()
                ));
            }
        }

        return new TelemetryEventResponseDtos.EventsResponse(received, accepted, rejected, errors);
    }

    /**
     * 이벤트 검증
     */
    private String validateEvent(TelemetryEventRequestDtos.Event eventDto) {
        if (eventDto.getEventId() == null) {
            return "eventId is required";
        }
        if (eventDto.getEventType() == null || eventDto.getEventType().isBlank()) {
            return "eventType is required";
        }
        if (eventDto.getTraceId() == null) {
            return "traceId is required";
        }
        if (eventDto.getUserId() == null || eventDto.getUserId().isBlank()) {
            return "userId is required";
        }
        if (eventDto.getDeptId() == null || eventDto.getDeptId().isBlank()) {
            return "deptId is required";
        }
        if (eventDto.getOccurredAt() == null || eventDto.getOccurredAt().isBlank()) {
            return "occurredAt is required";
        }
        if (eventDto.getPayload() == null) {
            return "payload is required";
        }

        // FEEDBACK 이벤트는 targetConversationId, targetTurnId 필수
        if ("FEEDBACK".equals(eventDto.getEventType())) {
            Object payload = eventDto.getPayload();
            if (payload instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> payloadMap = (java.util.Map<String, Object>) payload;
                if (payloadMap.get("targetConversationId") == null) {
                    return "targetConversationId is required for FEEDBACK event";
                }
                if (payloadMap.get("targetTurnId") == null) {
                    return "targetTurnId is required for FEEDBACK event";
                }
            }
        }

        return null;
    }

    /**
     * DTO를 엔티티로 변환
     */
    private TelemetryEvent toEntity(
        String source,
        String sentAt,
        TelemetryEventRequestDtos.Event eventDto
    ) {
        TelemetryEvent event = new TelemetryEvent();
        event.setEventId(eventDto.getEventId());
        event.setSource(source);
        
        // sentAt 파싱
        try {
            if (sentAt != null && !sentAt.isBlank()) {
                event.setSentAt(OffsetDateTime.parse(sentAt, ISO_FORMATTER).toInstant());
            } else {
                event.setSentAt(Instant.now());
            }
        } catch (Exception e) {
            log.warn("sentAt 파싱 실패, 현재 시간 사용: {}", sentAt, e);
            event.setSentAt(Instant.now());
        }

        event.setEventType(eventDto.getEventType());
        event.setTraceId(eventDto.getTraceId());
        event.setConversationId(eventDto.getConversationId());
        event.setTurnId(eventDto.getTurnId());
        event.setUserId(eventDto.getUserId());
        event.setDeptId(eventDto.getDeptId());

        // occurredAt 파싱
        try {
            event.setOccurredAt(OffsetDateTime.parse(eventDto.getOccurredAt(), ISO_FORMATTER).toInstant());
        } catch (Exception e) {
            log.warn("occurredAt 파싱 실패, 현재 시간 사용: {}", eventDto.getOccurredAt());
            event.setOccurredAt(Instant.now());
        }

        // payload를 JSON으로 변환
        event.setPayload(eventDto.getPayload());

        return event;
    }
}

