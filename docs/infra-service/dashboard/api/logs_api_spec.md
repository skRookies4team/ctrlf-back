## Infra Service - Admin Dashboard Logs API 명세서

Base URL: http://localhost:9003

---

# 1. Admin Dashboard Logs APIs

## 1.1 세부 로그 목록 조회

### ✔ URL

- GET /admin/dashboard/logs

### ✔ 설명

- AI 로그 데이터를 필터링 및 페이징하여 조회합니다.
- 관리자 대시보드에서 상세 로그를 확인할 때 사용합니다.
- 다양한 필터 조건을 조합하여 원하는 로그만 조회할 수 있습니다.

### ✔ 권한

`ROLE_ADMIN`

### ✔ 요청

Body: 없음

### Query Parameter

| key        | 설명              | value 타입 | 옵션     | Nullable | 예시         |
| ---------- | ----------------- | ---------- | -------- | -------- | ------------ |
| period     | 기간 (일)         | string     | optional | false    | "30"         |
| department | 부서명 필터       | string     | optional | true     | "총무팀"     |
| domain     | 도메인 ID 필터    | string     | optional | true     | "SECURITY"   |
| route      | 라우트 ID 필터    | string     | optional | true     | "RAG"        |
| model      | 모델명 필터       | string     | optional | true     | "gpt-4o-mini"|
| onlyError  | 에러만 보기       | boolean    | optional | false    | false        |
| hasPiiOnly | PII 포함만 보기   | boolean    | optional | false    | false        |
| page       | 페이지 번호       | number     | optional | false    | 0            |
| size       | 페이지 크기       | number     | optional | false    | 20           |

**period 값:**

- `7`: 최근 7일
- `30`: 최근 30일 (기본값)
- `90`: 최근 90일

**route 값:**

- `RAG`: RAG 라우팅
- `LLM`: LLM 직접 응답
- `INCIDENT`: 사고 처리
- `FAQ`: FAQ 응답
- 기타 라우트 ID

**onlyError 값:**

- `true`: 에러가 발생한 로그만 조회 (errorCode가 null이 아닌 경우)
- `false`: 모든 로그 조회 (기본값)

**hasPiiOnly 값:**

- `true`: PII가 감지된 로그만 조회 (hasPiiInput 또는 hasPiiOutput이 true인 경우)
- `false`: 모든 로그 조회 (기본값)

**page 값:**

- 0부터 시작 (기본값: 0)

**size 값:**

- 최대 100까지 가능 (기본값: 20)

### Response

| key              | 설명                | value 타입    | 옵션     | Nullable | 예시                    |
| ---------------- | ------------------- | ------------- | -------- | -------- | ----------------------- |
| content          | 로그 항목 배열       | array(object) | required | false    | 아래 표 참조            |
| totalElements    | 전체 로그 개수       | number         | required | false    | 150                     |
| totalPages       | 전체 페이지 수       | number         | required | false    | 8                       |
| page             | 현재 페이지 번호     | number         | required | false    | 0                       |
| size             | 페이지 크기          | number         | required | false    | 20                      |

content item

| key            | 설명                | value 타입 | 옵션     | Nullable | 예시                    |
| -------------- | ------------------- | ---------- | -------- | -------- | ----------------------- |
| id             | 로그 ID (UUID)       | string     | required | false    | "550e8400-..."          |
| createdAt      | 생성 시각            | string     | required | false    | "2025-01-09 10:21:34"   |
| userId         | 사용자 ID            | string     | required | false    | "user-uuid"             |
| userRole       | 사용자 역할          | string     | optional | true     | "EMPLOYEE"              |
| department     | 부서명               | string     | optional | true     | "총무팀"                |
| domain         | 도메인 ID            | string     | optional | true     | "SECURITY"              |
| route          | 라우트 ID            | string     | optional | true     | "RAG"                   |
| modelName      | 모델명               | string     | optional | true     | "gpt-4o-mini"           |
| hasPiiInput    | PII 입력 감지 여부    | boolean    | optional | true     | false                   |
| hasPiiOutput   | PII 출력 감지 여부    | boolean    | optional | true     | false                   |
| ragUsed        | RAG 사용 여부        | boolean    | optional | true     | true                    |
| ragSourceCount | RAG 소스 개수        | number     | optional | true     | 3                       |
| latencyMsTotal | 총 지연시간 (ms)     | number     | optional | true     | 420                     |
| errorCode      | 에러 코드            | string     | optional | true     | null (정상인 경우)      |

**Response 예시:**

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "createdAt": "2025-01-09 10:21:34",
      "userId": "user-uuid",
      "userRole": "EMPLOYEE",
      "department": "총무팀",
      "domain": "SECURITY",
      "route": "RAG",
      "modelName": "gpt-4o-mini",
      "hasPiiInput": false,
      "hasPiiOutput": false,
      "ragUsed": true,
      "ragSourceCount": 3,
      "latencyMsTotal": 420,
      "errorCode": null
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "createdAt": "2025-01-09 10:22:15",
      "userId": "user-uuid-2",
      "userRole": "EMPLOYEE",
      "department": "인사팀",
      "domain": "HR",
      "route": "LLM",
      "modelName": "gpt-4",
      "hasPiiInput": true,
      "hasPiiOutput": false,
      "ragUsed": false,
      "ragSourceCount": null,
      "latencyMsTotal": 1250,
      "errorCode": null
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "page": 0,
  "size": 20
}
```

### Status

| status  | response content |
| ------- | ----------------- |
| 200 OK  | 정상              |
| 401/403 | 인증/권한 오류    |
| 500     | 서버 오류         |

---

# 2. Internal AI Logs APIs (내부 API)

## 2.1 AI 로그 Bulk 수신

### ✔ URL

- POST /internal/ai/logs/bulk

### ✔ 설명

- AI 서버에서 정제된 로그를 bulk로 수신하여 DB에 저장합니다.
- 여러 로그를 한 번에 전송할 수 있으며, 부분 실패 시에도 성공한 로그는 저장됩니다.
- 내부 서비스 간 통신용 API이므로 X-Internal-Token 헤더가 필요합니다.

### ✔ 권한

내부 토큰 인증 (X-Internal-Token)

### ✔ 요청

Headers:

```
X-Internal-Token: {INTERNAL_TOKEN}
Content-Type: application/json
```

Body:

```json
{
  "logs": [
    {
      "createdAt": "2025-01-09T10:21:34Z",
      "userId": "user-uuid",
      "userRole": "EMPLOYEE",
      "department": "총무팀",
      "domain": "SECURITY",
      "route": "RAG",
      "modelName": "gpt-4o-mini",
      "hasPiiInput": false,
      "hasPiiOutput": false,
      "ragUsed": true,
      "ragSourceCount": 3,
      "latencyMsTotal": 420,
      "errorCode": null,
      "traceId": "trace-id",
      "conversationId": "conv-id",
      "turnId": 1
    }
  ]
}
```

### Request Body

| key                | 설명                | value 타입    | 옵션     | Nullable | 예시                    |
| ------------------ | ------------------- | ------------- | -------- | -------- | ----------------------- |
| logs               | 로그 항목 배열       | array(object) | required | false    | 아래 표 참조            |

logs item

| key            | 설명                | value 타입 | 옵션     | Nullable | 예시                    |
| -------------- | ------------------- | ---------- | -------- | -------- | ----------------------- |
| createdAt      | 생성 시각 (ISO 8601)| string     | required | false    | "2025-01-09T10:21:34Z"  |
| userId         | 사용자 ID            | string     | required | false    | "user-uuid"             |
| userRole       | 사용자 역할          | string     | optional | true     | "EMPLOYEE"              |
| department     | 부서명               | string     | optional | true     | "총무팀"                |
| domain         | 도메인 ID            | string     | optional | true     | "SECURITY"              |
| route          | 라우트 ID            | string     | optional | true     | "RAG"                   |
| modelName      | 모델명               | string     | optional | true     | "gpt-4o-mini"           |
| hasPiiInput    | PII 입력 감지 여부    | boolean    | optional | true     | false                   |
| hasPiiOutput   | PII 출력 감지 여부    | boolean    | optional | true     | false                   |
| ragUsed        | RAG 사용 여부        | boolean    | optional | true     | true                    |
| ragSourceCount | RAG 소스 개수        | number     | optional | true     | 3                       |
| latencyMsTotal | 총 지연시간 (ms)     | number     | optional | true     | 420                     |
| errorCode      | 에러 코드            | string     | optional | true     | null (정상인 경우)      |
| traceId        | 트레이스 ID          | string     | optional | true     | "trace-id"              |
| conversationId | 대화 ID              | string     | optional | true     | "conv-id"               |
| turnId         | 턴 ID                | number     | optional | true     | 1                       |

### Response

| key      | 설명                    | value 타입    | 옵션     | Nullable | 예시         |
| -------- | ----------------------- | ------------- | -------- | -------- | ------------ |
| received | 수신한 로그 개수         | number         | required | false    | 10           |
| saved    | 성공적으로 저장된 개수   | number         | required | false    | 9            |
| failed   | 저장 실패한 개수         | number         | required | false    | 1            |
| errors   | 실패한 로그의 에러 정보  | array(object) | required | false    | 아래 표 참조 |

errors item

| key       | 설명              | value 타입 | 옵션     | Nullable | 예시              |
| --------- | ----------------- | ---------- | -------- | -------- | ----------------- |
| index     | 실패한 로그 인덱스 | number     | required | false    | 5                 |
| errorCode | 에러 코드          | string     | required | false    | "SAVE_ERROR"       |
| message   | 에러 메시지        | string     | required | false    | "Validation failed" |

**Response 예시:**

```json
{
  "received": 10,
  "saved": 9,
  "failed": 1,
  "errors": [
    {
      "index": 5,
      "errorCode": "SAVE_ERROR",
      "message": "Validation failed: userId is required"
    }
  ]
}
```

### Status

| status  | response content |
| ------- | ----------------- |
| 200 OK  | 정상 (부분 실패 포함) |
| 400     | 잘못된 요청        |
| 401     | 내부 토큰 오류     |
| 500     | 서버 오류          |

---

## 주의사항

1. **데이터 소스**: 관리자 대시보드 로그는 `ai_log` 테이블의 데이터를 기반으로 조회됩니다.
2. **로그 저장**: AI 서버에서 정제된 로그만 저장되며, 원본 로그는 Elasticsearch에 저장됩니다.
3. **필터링**: 여러 필터 조건을 동시에 사용할 수 있으며, AND 조건으로 결합됩니다.
4. **페이징**: 페이지 번호는 0부터 시작하며, 페이지 크기는 최대 100까지 가능합니다.
5. **에러 처리**: Bulk 수신 시 일부 로그가 실패해도 성공한 로그는 저장되며, 실패한 로그의 정보는 errors 배열에 포함됩니다.
6. **내부 API**: `/internal/ai/logs/bulk`는 내부 서비스 간 통신용이므로 외부에서 직접 호출하지 않습니다.

---

## 관련 문서

- [Admin Dashboard Metrics API](./metrics_api_spec.md)
- [RAG Documents API](../rag/api/API_SPECIFICATION.md)
- [Telemetry API](../telemetry/api/telemetry_api_spec.md) (예정)

