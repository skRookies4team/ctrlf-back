-- Telemetry 이벤트 테이블 생성
CREATE SCHEMA IF NOT EXISTS telemetry;
SET search_path = telemetry;

CREATE TABLE IF NOT EXISTS telemetry.telemetry_event (
  event_id uuid PRIMARY KEY,
  source varchar(50) NOT NULL,
  sent_at timestamptz NOT NULL,
  event_type varchar(30) NOT NULL,
  trace_id uuid NOT NULL,
  conversation_id varchar(100),
  turn_id int,
  user_id varchar(64) NOT NULL,
  dept_id varchar(64) NOT NULL,
  occurred_at timestamptz NOT NULL,
  payload jsonb NOT NULL,
  received_at timestamptz NOT NULL DEFAULT now()
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_telemetry_event_occurred_at ON telemetry.telemetry_event(occurred_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_event_dept_occurred ON telemetry.telemetry_event(dept_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_event_type_occurred ON telemetry.telemetry_event(event_type, occurred_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_event_conversation_turn ON telemetry.telemetry_event(conversation_id, turn_id);

