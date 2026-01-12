package com.ctrlf.infra.telemetry.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_event", schema = "telemetry")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEventEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String alertType;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String title;

    // 🔥 JSONB는 Map/Object 절대 금지 → JsonNode만 허용
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;

    @Column(nullable = false)
    private Instant receivedAt;
}
