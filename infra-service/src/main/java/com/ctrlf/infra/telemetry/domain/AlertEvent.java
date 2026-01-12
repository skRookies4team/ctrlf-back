package com.ctrlf.infra.telemetry.domain;

import java.time.OffsetDateTime;
import java.util.Map;

public record AlertEvent(
    String type,
    String status,
    String title,
    String severity,
    String source,
    Map<String, String> labels,
    Map<String, String> annotations,
    OffsetDateTime occurredAt,
    Map<String, Object> rawPayload     // 🔥 Grafana 전체 payload
) {}
