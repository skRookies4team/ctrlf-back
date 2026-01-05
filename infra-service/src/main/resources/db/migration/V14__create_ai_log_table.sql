-- V14: AI 로그 테이블 생성
SET search_path = infra;

-- AI 로그 테이블 생성
CREATE TABLE IF NOT EXISTS "ai_log" (
  "id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  "created_at" timestamptz NOT NULL,
  "user_id" varchar(64) NOT NULL,
  "user_role" varchar(50),
  "department" varchar(100),
  "domain" varchar(50),
  "route" varchar(50),
  "model_name" varchar(100),
  "has_pii_input" boolean,
  "has_pii_output" boolean,
  "rag_used" boolean,
  "rag_source_count" integer,
  "latency_ms_total" bigint,
  "error_code" varchar(50),
  "trace_id" varchar(200),
  "conversation_id" varchar(100),
  "turn_id" integer,
  "received_at" timestamptz NOT NULL DEFAULT now()
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS "idx_ai_log_created_at" ON "ai_log" ("created_at");
CREATE INDEX IF NOT EXISTS "idx_ai_log_department_created" ON "ai_log" ("department", "created_at");
CREATE INDEX IF NOT EXISTS "idx_ai_log_domain_created" ON "ai_log" ("domain", "created_at");
CREATE INDEX IF NOT EXISTS "idx_ai_log_route_created" ON "ai_log" ("route", "created_at");
CREATE INDEX IF NOT EXISTS "idx_ai_log_model_created" ON "ai_log" ("model_name", "created_at");
CREATE INDEX IF NOT EXISTS "idx_ai_log_error_code" ON "ai_log" ("error_code") WHERE "error_code" IS NOT NULL;
CREATE INDEX IF NOT EXISTS "idx_ai_log_trace_id" ON "ai_log" ("trace_id") WHERE "trace_id" IS NOT NULL;
CREATE INDEX IF NOT EXISTS "idx_ai_log_conversation_turn" ON "ai_log" ("conversation_id", "turn_id") WHERE "conversation_id" IS NOT NULL;

-- 코멘트 추가
COMMENT ON TABLE "ai_log" IS 'AI에서 정제된 로그 (관리자 대시보드용)';
COMMENT ON COLUMN "ai_log"."id" IS 'AI 로그 PK';
COMMENT ON COLUMN "ai_log"."created_at" IS '생성 시각 (AI에서 발생한 시각)';
COMMENT ON COLUMN "ai_log"."user_id" IS '사용자 ID';
COMMENT ON COLUMN "ai_log"."user_role" IS '사용자 역할 (EMPLOYEE, ADMIN 등)';
COMMENT ON COLUMN "ai_log"."department" IS '부서명';
COMMENT ON COLUMN "ai_log"."domain" IS '도메인 ID';
COMMENT ON COLUMN "ai_log"."route" IS '라우트 ID (RAG, LLM, INCIDENT, FAQ 등)';
COMMENT ON COLUMN "ai_log"."model_name" IS '모델명';
COMMENT ON COLUMN "ai_log"."has_pii_input" IS 'PII 입력 감지 여부';
COMMENT ON COLUMN "ai_log"."has_pii_output" IS 'PII 출력 감지 여부';
COMMENT ON COLUMN "ai_log"."rag_used" IS 'RAG 사용 여부';
COMMENT ON COLUMN "ai_log"."rag_source_count" IS 'RAG 소스 개수';
COMMENT ON COLUMN "ai_log"."latency_ms_total" IS '총 지연시간 (밀리초)';
COMMENT ON COLUMN "ai_log"."error_code" IS '에러 코드 (에러 발생 시)';
COMMENT ON COLUMN "ai_log"."trace_id" IS '트레이스 ID';
COMMENT ON COLUMN "ai_log"."conversation_id" IS '대화 ID';
COMMENT ON COLUMN "ai_log"."turn_id" IS '턴 ID';
COMMENT ON COLUMN "ai_log"."received_at" IS '백엔드 수신 시각';

