CREATE SCHEMA IF NOT EXISTS telemetry;

CREATE TABLE telemetry.alert_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    alert_type VARCHAR(50) NOT NULL,     -- GRAFANA
    severity   VARCHAR(20) NOT NULL,     -- CRITICAL
    source     VARCHAR(50) NOT NULL,     -- prometheus
    status     VARCHAR(30) NOT NULL,     -- firing / resolved
    title      VARCHAR(300) NOT NULL,

    payload    JSONB NOT NULL,

    received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_alert_event_received_at
    ON telemetry.alert_event (received_at DESC);

CREATE INDEX idx_alert_event_severity
    ON telemetry.alert_event (severity);
