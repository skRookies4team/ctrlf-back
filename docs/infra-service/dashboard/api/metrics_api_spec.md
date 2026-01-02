## Infra Service - Admin Dashboard Metrics API 명세서

Base URL: http://localhost:9003

---

# 1. Admin Dashboard Metrics APIs

## 1.1 보안 지표 조회

### ✔ URL

- GET /admin/dashboard/metrics/security

### ✔ 설명

- PII 차단 수, 외부 도메인 차단 수, PII 추이를 조회합니다.
- 텔레메트리 이벤트 데이터를 기반으로 보안 관련 지표를 집계합니다.

### ✔ 권한

`ROLE_ADMIN`

### ✔ 요청

Body: 없음

### Query Parameter

| key     | 설명           | value 타입 | 옵션     | Nullable | 예시  |
| ------- | -------------- | ---------- | -------- | -------- | ----- |
| period  | 기간           | string     | optional | false    | "30d" |
| dept    | 부서 필터      | string     | optional | false    | "all" |
| refresh | 캐시 무시 여부 | boolean    | optional | false    | false |

**period 값:**

- `today`: 오늘
- `7d`: 최근 7일
- `30d`: 최근 30일 (기본값)
- `90d`: 최근 90일

**dept 값:**

- `all`: 전체 부서 (기본값)
- 부서 ID: 특정 부서만 조회

### Response

| key                      | 설명                | value 타입    | 옵션     | Nullable | 예시         |
| ------------------------ | ------------------- | ------------- | -------- | -------- | ------------ |
| piiBlockCount            | PII 차단 횟수       | number        | required | false    | 42           |
| externalDomainBlockCount | 외부 도메인 차단 수 | number        | required | false    | 3            |
| piiTrend                 | PII 감지 추이       | array(object) | required | false    | 아래 표 참조 |

piiTrend item

| key              | 설명                   | value 타입 | 옵션     | Nullable | 예시         |
| ---------------- | ---------------------- | ---------- | -------- | -------- | ------------ |
| bucketStart      | 기간 레이블            | string     | required | false    | "2024-01-01" |
| inputDetectRate  | 입력 PII 감지 비율 (%) | number     | required | false    | 4.1          |
| outputDetectRate | 출력 PII 감지 비율 (%) | number     | required | false    | 2.9          |

**Response 예시:**

```json
{
  "piiBlockCount": 42,
  "externalDomainBlockCount": 3,
  "piiTrend": [
    {
      "bucketStart": "2024-01-01",
      "inputDetectRate": 4.1,
      "outputDetectRate": 2.9
    },
    {
      "bucketStart": "2024-01-08",
      "inputDetectRate": 3.8,
      "outputDetectRate": 2.5
    }
  ]
}
```

### Status

| status  | response content |
| ------- | ---------------- |
| 200 OK  | 정상             |
| 401/403 | 인증/권한 오류   |
| 500     | 서버 오류        |

---

## 1.2 성능 지표 조회

### ✔ URL

- GET /admin/dashboard/metrics/performance

### ✔ 설명

- 불만족도, 재질문률, OOS 카운트, 지연시간 히스토그램, 모델별 평균 지연시간을 조회합니다.
- 텔레메트리 이벤트 데이터를 기반으로 성능 관련 지표를 집계합니다.

### ✔ 권한

`ROLE_ADMIN`

### ✔ 요청

Body: 없음

### Query Parameter

| key     | 설명           | value 타입 | 옵션     | Nullable | 예시  |
| ------- | -------------- | ---------- | -------- | -------- | ----- |
| period  | 기간           | string     | optional | false    | "30d" |
| dept    | 부서 필터      | string     | optional | false    | "all" |
| refresh | 캐시 무시 여부 | boolean    | optional | false    | false |

**period 값:**

- `today`: 오늘
- `7d`: 최근 7일
- `30d`: 최근 30일 (기본값)
- `90d`: 최근 90일

**dept 값:**

- `all`: 전체 부서 (기본값)
- 부서 ID: 특정 부서만 조회

### Response

| key              | 설명                 | value 타입    | 옵션     | Nullable | 예시                                                                    |
| ---------------- | -------------------- | ------------- | -------- | -------- | ----------------------------------------------------------------------- |
| dislikeRate      | 답변 불만족 비율     | number        | required | false    | 0.15 (15%)                                                              |
| repeatRate       | 재질문 비율          | number        | required | false    | 0.08 (8%)                                                               |
| repeatDefinition | 재질문률 정의 설명   | string        | required | false    | "MVP: same conversation, within last 3 turns, same intentMain repeated" |
| oosCount         | Out-of-scope 응답 수 | number        | required | false    | 25                                                                      |
| latencyHistogram | 응답 시간 분포       | array(object) | required | false    | 아래 표 참조                                                            |
| modelLatency     | 모델별 평균 지연시간 | array(object) | required | false    | 아래 표 참조                                                            |

latencyHistogram item

| key   | 설명                | value 타입 | 옵션     | Nullable | 예시      |
| ----- | ------------------- | ---------- | -------- | -------- | --------- |
| range | 시간 구간 레이블    | string     | required | false    | "0-500ms" |
| count | 해당 구간 응답 건수 | number     | required | false    | 320       |

**latencyHistogram 구간:**

- `0-500ms`: 0ms 이상 500ms 미만
- `0.5-1s`: 500ms 이상 1초 미만
- `1-2s`: 1초 이상 2초 미만
- `2s+`: 2초 이상

modelLatency item

| key          | 설명               | value 타입 | 옵션     | Nullable | 예시          |
| ------------ | ------------------ | ---------- | -------- | -------- | ------------- |
| model        | 모델 이름          | string     | required | false    | "gpt-4o-mini" |
| avgLatencyMs | 평균 지연시간 (ms) | number     | required | false    | 1250.5        |

**Response 예시:**

```json
{
  "dislikeRate": 0.15,
  "repeatRate": 0.08,
  "repeatDefinition": "MVP: same conversation, within last 3 turns, same intentMain repeated",
  "oosCount": 25,
  "latencyHistogram": [
    {
      "range": "0-500ms",
      "count": 320
    },
    {
      "range": "0.5-1s",
      "count": 145
    },
    {
      "range": "1-2s",
      "count": 89
    },
    {
      "range": "2s+",
      "count": 12
    }
  ],
  "modelLatency": [
    {
      "model": "gpt-4o-mini",
      "avgLatencyMs": 1250.5
    },
    {
      "model": "gpt-4",
      "avgLatencyMs": 2100.3
    }
  ]
}
```

### Status

| status  | response content |
| ------- | ---------------- |
| 200 OK  | 정상             |
| 401/403 | 인증/권한 오류   |
| 500     | 서버 오류        |

---

## 주의사항

1. **데이터 소스**: 모든 지표는 `telemetry_event` 테이블의 이벤트 데이터를 기반으로 집계됩니다.
2. **이벤트 타입**:
   - 보안 지표: `SECURITY` 이벤트 타입 사용
   - 성능 지표: `CHAT_TURN`, `FEEDBACK` 이벤트 타입 사용
3. **PII 추이**: 주간별로 집계되며, ISO 주 기준으로 버킷이 생성됩니다.
4. **재질문률**: 동일 conversation 내에서 최근 3턴 내에 같은 `intentMain`이 반복된 경우를 재질문으로 간주합니다.
5. **지연시간**: `latencyMsTotal` (전체 지연시간)과 `latencyMsLlm` (LLM 지연시간)을 사용합니다.
6. **부서 필터**: `dept` 파라미터가 `all`이 아닌 경우, 해당 부서의 이벤트만 집계됩니다.

---

## 관련 문서

- [Chat Service API 명세](../chat-service/chat/api/chat_api_spec.md)
- [Education Service API 명세](../education-service/education/api/education_api_spec.md)
