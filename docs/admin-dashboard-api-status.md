# Admin Dashboard API 구현 현황

## 개요

`AdminDashboardView.tsx` 컴포넌트에서 필요한 백엔드 API의 구현 현황을 정리한 문서입니다.

**기준 문서**: `ctrlf-back/docs` 폴더의 API 명세서

---

## 1. 챗봇 탭 (Chatbot Tab)

### 현재 상태: ❌ **미구현** (Mock 데이터 사용 중)

### 필요한 API 목록

#### 1.1 대시보드 요약 통계 조회
- **엔드포인트**: `GET /admin/dashboard/chatbot/summary`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90` (기본값: `30`)
  - `department` (optional): 부서명 (예: "총무팀")
- **응답 필드**:
  - Primary KPI:
    - `todayQuestions`: 오늘 질문 수
    - `avgLatency`: 평균 응답 시간 (ms)
    - `piiRatio`: PII 감지 비율 (%)
    - `errorRatio`: 에러율 (%)
  - Secondary KPI:
    - `weekQuestions`: 최근 7일 질문 수
    - `avgLatency`: 평균 응답 시간 (ms)
    - `piiRatio`: PII 감지 비율 (%)
    - `errorRatio`: 에러율 (%)
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 1.2 질문 수/에러율 추이 조회
- **엔드포인트**: `GET /admin/dashboard/chatbot/volume`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `ChatbotVolumePoint[]`
  ```typescript
  {
    label: string;        // "월", "화", "수" 등
    count: number;        // 질문 수
    errorRatio?: number;  // 에러율 (0~1)
  }[]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 1.3 도메인별 질문 비율 조회
- **엔드포인트**: `GET /admin/dashboard/chatbot/domain-share`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `ChatbotDomainShare[]`
  ```typescript
  {
    id: string;           // 도메인 ID
    domainLabel: string;  // "규정", "FAQ", "교육", "퀴즈", "기타"
    ratio: number;        // 비율 (%)
  }[]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 1.4 라우트별 질문 비율 조회
- **엔드포인트**: `GET /admin/dashboard/chatbot/route-share`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `ChatbotRouteShare[]`
  ```typescript
  {
    id: string;           // 라우트 ID
    routeLabel: string;  // "RAG", "LLM", "Incident", "FAQ 템플릿" 등
    ratio: number;        // 비율 (%)
  }[]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 1.5 인기 키워드 조회
- **엔드포인트**: `GET /admin/dashboard/chatbot/popular-keywords`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
  - `limit` (optional): 조회 개수 (기본값: `5`)
- **응답 형식**: `PopularKeyword[]`
  ```typescript
  {
    keyword: string;  // 키워드
    count: number;    // 질문 수
  }[]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

---

## 2. 교육 탭 (Education Tab)

### 현재 상태: ✅ **구현됨** (docs 기준)

### 필요한 API 목록

#### 2.1 대시보드 요약 통계 조회
- **엔드포인트**: `GET /admin/dashboard/education/summary`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90` (기본값: `30`)
  - `department` (optional): 부서명
- **응답 필드**:
  ```json
  {
    "overallAverageCompletionRate": 85.5,    // 전체 평균 이수율 (%)
    "nonCompleterCount": 15,                 // 미이수자 수
    "mandatoryEducationAverage": 90.2,       // 4대 의무교육 평균 이수율 (%)
    "jobEducationAverage": 78.3               // 직무교육 평균 이수율 (%)
  }
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `education-service/education/api/education_api_spec.md` (3.1)

#### 2.2 4대 의무교육 이수율 조회
- **엔드포인트**: `GET /admin/dashboard/education/mandatory-completion`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 필드**:
  ```json
  {
    "sexualHarassmentPrevention": 95.0,  // 성희롱 예방교육 이수율 (%)
    "personalInfoProtection": 92.5,      // 개인정보보호 교육 이수율 (%)
    "workplaceBullying": 88.3,           // 직장 내 괴롭힘 예방 이수율 (%)
    "disabilityAwareness": 90.1          // 장애인 인식개선 이수율 (%)
  }
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `education-service/education/api/education_api_spec.md` (3.2)

#### 2.3 직무교육 이수 현황 조회
- **엔드포인트**: `GET /admin/dashboard/education/job-completion`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `JobCourseSummary[]`
  ```json
  [
    {
      "educationId": "550e8400-e29b-41d4-a716-446655440000",
      "title": "신입사원 온보딩 교육",
      "status": "진행 중",  // "진행 중" | "이수 완료"
      "learnerCount": 25
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `education-service/education/api/education_api_spec.md` (3.3)

#### 2.4 부서별 이수율 현황 조회
- **엔드포인트**: `GET /admin/dashboard/education/department-completion`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - **참고**: 부서 필터는 적용되지 않음 (부서별 통계이므로)
- **응답 형식**: `DeptEducationRow[]`
  ```json
  [
    {
      "department": "총무팀",
      "targetCount": 50,        // 대상자 수
      "completerCount": 45,     // 이수자 수
      "completionRate": 90.0,  // 이수율 (%)
      "nonCompleterCount": 5    // 미이수자 수
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `education-service/education/api/education_api_spec.md` (3.4)

---

## 3. 퀴즈 탭 (Quiz Tab)

### 현재 상태: ✅ **부분 구현됨** (3/4 API 구현)

### 필요한 API 목록

#### 3.1 대시보드 요약 통계 조회
- **엔드포인트**: `GET /admin/dashboard/quiz/summary`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90` (기본값: `30`)
  - `department` (optional): 부서명
- **응답 필드**:
  ```json
  {
    "overallAverageScore": 84.0,    // 전체 평균 점수
    "participantCount": 176,        // 응시자 수
    "passRate": 78.0,               // 통과율 (80점↑) (%)
    "participationRate": 73.0       // 퀴즈 응시율 (%)
  }
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `education-service/quiz/api/quiz_api_spec.md` (2.1)

#### 3.2 부서별 평균 점수 조회
- **엔드포인트**: `GET /admin/dashboard/quiz/department-scores`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `DeptQuizScoreRow[]`
  ```json
  [
    {
      "department": "인사팀",
      "averageScore": 89.0,        // 평균 점수
      "participantCount": 13       // 응시자 수
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `education-service/quiz/api/quiz_api_spec.md` (2.2)

#### 3.3 퀴즈별 통계 조회
- **엔드포인트**: `GET /admin/dashboard/quiz/quiz-stats`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `QuizSummaryRow[]`
  ```json
  [
    {
      "educationId": "550e8400-e29b-41d4-a716-446655440000",
      "quizTitle": "개인정보보호 퀴즈",
      "attemptNo": 1,              // 회차
      "averageScore": 86.0,        // 평균 점수
      "attemptCount": 57,           // 응시 수
      "passRate": 81.0              // 통과율 (%)
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `education-service/quiz/api/quiz_api_spec.md` (2.3)

#### 3.4 오답 비율이 높은 문제 조회
- **엔드포인트**: `GET /admin/dashboard/quiz/difficult-questions`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
  - `limit` (optional): 조회 개수 (기본값: `5`)
- **응답 형식**: `DifficultQuestion[]`
  ```json
  [
    {
      "id": "question-id",
      "title": "문제 제목",
      "wrongRate": 45.2  // 오답률 (%)
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현 (docs에 명세 없음)
- **참고**: 프론트엔드에서 사용 중이지만 API 명세가 없음

---

## 4. 지표 탭 (Metrics Tab)

### 현재 상태: ❌ **미구현** (Mock 데이터 사용 중)

### 필요한 API 목록

#### 4.1 보안/PII 지표 조회
- **엔드포인트**: `GET /admin/dashboard/metrics/security`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `MetricItem[]`
  ```json
  [
    {
      "id": "pii-detected",
      "label": "PII 감지 건수",
      "value": "42건",
      "description": "입력/출력에서 개인정보가 탐지된 건수"
    },
    {
      "id": "security-blocked",
      "label": "보안 차단 이벤트",
      "value": "3건",
      "description": "보안 정책 위반으로 차단된 이벤트 수"
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 4.2 성능/장애 지표 조회
- **엔드포인트**: `GET /admin/dashboard/metrics/quality`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `MetricItem[]`
  ```json
  [
    {
      "id": "avg-latency",
      "label": "평균 응답 시간",
      "value": "420ms",
      "description": "전체 요청의 평균 응답 시간"
    },
    {
      "id": "error-count",
      "label": "에러 건수",
      "value": "12건",
      "description": "에러가 발생한 요청 건수"
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 4.3 PII 감지 추이 조회
- **엔드포인트**: `GET /admin/dashboard/metrics/pii-trend`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `PiiTrendPoint[]`
  ```json
  [
    {
      "label": "월",
      "inputRatio": 4.1,   // 입력 PII 비율 (%)
      "outputRatio": 2.9   // 출력 PII 비율 (%)
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 4.4 응답 시간 분포 조회
- **엔드포인트**: `GET /admin/dashboard/metrics/latency-distribution`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `LatencyBucket[]`
  ```json
  [
    {
      "label": "0-500ms",
      "count": 320  // 해당 구간 응답 건수
    },
    {
      "label": "500-1000ms",
      "count": 145
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 4.5 모델별 평균 응답 시간 조회
- **엔드포인트**: `GET /admin/dashboard/metrics/model-latency`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `ModelLatency[]`
  ```json
  [
    {
      "id": "gpt-mini",
      "modelLabel": "gpt-4o-mini",
      "avgMs": 410  // 평균 응답 시간 (ms)
    }
  ]
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

---

## 5. 로그 탭 (Logs Tab)

### 현재 상태: ❌ **미구현** (Mock 데이터 사용 중)

### 필요한 API 목록

#### 5.1 세부 로그 목록 조회
- **엔드포인트**: `GET /admin/dashboard/logs`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
  - `domain` (optional): 도메인 ID
  - `route` (optional): 라우트 ID
  - `model` (optional): 모델 ID
  - `onlyError` (optional): `true` | `false` (에러만 보기)
  - `hasPiiOnly` (optional): `true` | `false` (PII 포함만 보기)
  - `page` (optional): 페이지 번호 (기본값: `0`)
  - `size` (optional): 페이지 크기 (기본값: `20`)
- **응답 형식**: `PageResponse<LogListItem>`
  ```json
  {
    "content": [
      {
        "id": "log-id",
        "createdAt": "2025-12-09 10:21:34",
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
      }
    ],
    "totalElements": 150,
    "totalPages": 8,
    "page": 0,
    "size": 20
  }
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

#### 5.2 RAG 갭 분석 조회
- **엔드포인트**: `GET /admin/dashboard/logs/rag-gap`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
- **응답 형식**: `AdminRagGapView`에서 사용하는 형식
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현
- **참고**: `AdminRagGapView` 컴포넌트에서 사용

#### 5.3 PII 점검 결과 조회
- **엔드포인트**: `GET /admin/dashboard/logs/pii-report`
- **Query Parameters**:
  - `period` (optional): `7` | `30` | `90`
  - `department` (optional): 부서명
  - `domain` (optional): 도메인 ID
  - `route` (optional): 라우트 ID
  - `model` (optional): 모델 ID
- **응답 형식**: `PiiReport`
  ```json
  {
    "riskLevel": "warning",  // "none" | "warning" | "high"
    "summaryLines": [
      "요약 문장 블록 1",
      "요약 문장 블록 2"
    ],
    "detectedItems": [
      "탐지된 개인정보 항목 1",
      "탐지된 개인정보 항목 2"
    ],
    "recommendedActions": [
      "권장 조치 1",
      "권장 조치 2"
    ],
    "maskedText": "마스킹된 텍스트 (optional)",
    "modelName": "pii-detection-model",
    "analyzedAt": "2025-12-09T10:21:34Z",
    "traceId": "trace-id"
  }
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현

---

## 6. 계정/롤 관리 탭 (Accounts Tab)

### 현재 상태: ❌ **미구현** (Mock 데이터 사용 중)

### 필요한 API 목록

#### 6.1 사용자 목록 조회
- **엔드포인트**: `GET /admin/users`
- **Query Parameters**:
  - `department` (optional): 부서 코드 (예: "GA", "PLAN")
  - `role` (optional): 역할 (예: "EMPLOYEE", "VIDEO_CREATOR")
  - `keyword` (optional): 검색어 (이름 또는 사번)
  - `page` (optional): 페이지 번호 (기본값: `0`)
  - `size` (optional): 페이지 크기 (기본값: `20`)
- **응답 형식**: `PageResponse<AdminUserSummary>`
  ```json
  {
    "content": [
      {
        "id": "user-uuid",
        "name": "홍길동",
        "employeeNo": "EMP001",
        "deptCode": "GA",
        "deptName": "총무팀",
        "roles": ["EMPLOYEE", "VIDEO_CREATOR"],
        "creatorType": "DEPT_CREATOR",  // "DEPT_CREATOR" | "GLOBAL_CREATOR" | null
        "creatorDeptScope": ["GA", "PLAN"]  // 제작 가능 부서 목록
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "page": 0,
    "size": 20
  }
  ```
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현
- **참고**: Keycloak 연동 필요

#### 6.2 사용자 상세 정보 조회
- **엔드포인트**: `GET /admin/users/{userId}`
- **Path Parameters**:
  - `userId`: 사용자 UUID
- **응답 형식**: `AdminUserSummary` (6.1과 동일)
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현
- **참고**: Keycloak 연동 필요

#### 6.3 사용자 역할 업데이트
- **엔드포인트**: `PATCH /admin/users/{userId}/roles`
- **Path Parameters**:
  - `userId`: 사용자 UUID
- **Request Body**:
  ```json
  {
    "roles": ["EMPLOYEE", "VIDEO_CREATOR", "CONTENTS_REVIEWER"]
  }
  ```
- **응답**: `200 OK` (업데이트 성공)
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현
- **참고**: Keycloak 역할 업데이트 필요

#### 6.4 영상 제작 권한 업데이트
- **엔드포인트**: `PATCH /admin/users/{userId}/creator-permissions`
- **Path Parameters**:
  - `userId`: 사용자 UUID
- **Request Body**:
  ```json
  {
    "creatorType": "DEPT_CREATOR",  // "DEPT_CREATOR" | "GLOBAL_CREATOR" | null
    "creatorDeptScope": ["GA", "PLAN"]  // 제작 가능 부서 목록
  }
  ```
- **응답**: `200 OK` (업데이트 성공)
- **권한**: `ROLE_ADMIN`
- **상태**: ❌ 미구현
- **참고**: 백엔드 DB에 저장 필요 (Keycloak에는 없음)

---

## 7. 사규 관리 탭 (Policy Tab)

### 현재 상태: ✅ **구현됨** (RAG Documents API 사용)

### 필요한 API 목록

#### 7.1 사규 목록 조회
- **엔드포인트**: `GET /rag/documents/policies`
- **Query Parameters**: (RAG Documents API 참고)
- **권한**: `ROLE_USER` (또는 `ROLE_ADMIN`)
- **상태**: ✅ 구현됨
- **참고 문서**: `infra-service/rag/api/API_SPECIFICATION.md` (4.1)

#### 7.2 사규 상세 조회
- **엔드포인트**: `GET /rag/documents/policies/{documentId}`
- **Path Parameters**:
  - `documentId`: 사규 문서 ID
- **권한**: `ROLE_USER` (또는 `ROLE_ADMIN`)
- **상태**: ✅ 구현됨
- **참고 문서**: `infra-service/rag/api/API_SPECIFICATION.md` (4.2)

#### 7.3 사규 생성
- **엔드포인트**: `POST /rag/documents/policies`
- **Request Body**: (RAG Documents API 참고)
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `infra-service/rag/api/API_SPECIFICATION.md` (4.3)

#### 7.4 사규 버전 생성
- **엔드포인트**: `POST /rag/documents/policies/{documentId}/versions`
- **Path Parameters**:
  - `documentId`: 사규 문서 ID
- **Request Body**: (RAG Documents API 참고)
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `infra-service/rag/api/API_SPECIFICATION.md` (4.4)

#### 7.5 사규 버전 수정
- **엔드포인트**: `PATCH /rag/documents/policies/{documentId}/versions/{version}`
- **Path Parameters**:
  - `documentId`: 사규 문서 ID
  - `version`: 버전 번호
- **Request Body**: (RAG Documents API 참고)
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `infra-service/rag/api/API_SPECIFICATION.md` (4.5)

#### 7.6 사규 상태 변경
- **엔드포인트**: `PATCH /rag/documents/policies/{documentId}/versions/{version}/status`
- **Path Parameters**:
  - `documentId`: 사규 문서 ID
  - `version`: 버전 번호
- **Request Body**: (RAG Documents API 참고)
- **권한**: `ROLE_ADMIN`
- **상태**: ✅ 구현됨
- **참고 문서**: `infra-service/rag/api/API_SPECIFICATION.md` (4.6)

---

## 요약 통계

### 탭별 구현 현황

| 탭 | 구현 상태 | 구현된 API | 미구현 API | 비고 |
|---|---|---|---|---|
| **챗봇** | ❌ 미구현 | 0/5 | 5 | Mock 데이터 사용 중 |
| **교육** | ✅ 구현됨 | 4/4 | 0 | 완전 구현 |
| **퀴즈** | ⚠️ 부분 구현 | 3/4 | 1 | 오답 비율 API 미구현 |
| **지표** | ❌ 미구현 | 0/5 | 5 | Mock 데이터 사용 중 |
| **로그** | ❌ 미구현 | 0/3 | 3 | Mock 데이터 사용 중 |
| **계정/롤** | ❌ 미구현 | 0/4 | 4 | Mock 데이터 사용 중 |
| **사규** | ✅ 구현됨 | 6/6 | 0 | RAG API 사용 |

### 전체 통계

- **총 API 수**: 31개
- **구현됨**: 13개 (42%)
- **미구현**: 18개 (58%)

### 우선순위별 구현 필요 API

#### 높은 우선순위 (사용자 가시성 높음)
1. 챗봇 탭 통계 API (5개)
2. 지표 탭 API (5개)
3. 로그 탭 API (3개)

#### 중간 우선순위
4. 계정/롤 관리 API (4개) - Keycloak 연동 필요

#### 낮은 우선순위
5. 퀴즈 탭 오답 비율 API (1개) - 기존 API로 대체 가능

---

## 참고 문서

- **Education API**: `education-service/education/api/education_api_spec.md`
- **Quiz API**: `education-service/quiz/api/quiz_api_spec.md`
- **RAG Documents API**: `infra-service/rag/api/API_SPECIFICATION.md`
- **Admin Dashboard Flow**:
  - `education-service/education/flow/admin_dashboard_education_flow.md`
  - `education-service/quiz/flow/admin_dashboard_quiz_flow.md`

---

## 업데이트 이력

- 2025-01-XX: 초기 작성

