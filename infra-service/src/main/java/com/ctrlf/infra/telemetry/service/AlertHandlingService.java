package com.ctrlf.infra.telemetry.service;

import com.ctrlf.infra.telemetry.domain.AlertEvent;
import com.ctrlf.infra.telemetry.entity.AlertEventEntity;
import com.ctrlf.infra.telemetry.repository.AlertEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AlertHandlingService {

    private final AlertEventRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handle(AlertEvent event) {

        AlertEventEntity entity = AlertEventEntity.builder()
            .alertType(event.type())
            .severity(event.severity())
            .source(event.source())
            .status(event.status())
            .title(event.title())
            .receivedAt(Instant.now())
            .payload(objectMapper.valueToTree(event.rawPayload()))   // 🔥 Map → JsonNode 변환
            .build();

        repository.save(entity);
    }
}
