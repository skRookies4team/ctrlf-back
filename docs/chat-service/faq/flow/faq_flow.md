# FAQ 서비스 플로우

## 개요

FAQ Service는 FAQ(자주 묻는 질문) 관리 및 조회 기능을 제공하는 서비스입니다. 관리자는 수동으로 FAQ를 생성하거나, AI를 활용하여 FAQ 후보에서 자동으로 초안을 생성할 수 있습니다. 생성된 초안은 검토 후 승인/반려되며, 승인된 FAQ는 사용자에게 노출됩니다.

## 엔티티 구조

```
FAQ (자주 묻는 질문)
├── id: UUID
├── question: 질문 내용
├── answer: 답변 내용
├── domain: 도메인 (SECURITY, POLICY, EDUCATION 등)
├── isActive: 활성 여부
├── priority: 우선순위 (1~5)
├── publishedAt: 게시 시간
├── createdAt: 생성 시간
└── updatedAt: 수정 시간

FaqCandidate (FAQ 후보)
├── id: UUID
├── canonicalQuestion: 표준 질문
├── domain: 도메인
├── questionCount7d: 7일간 질문 횟수
├── questionCount30d: 30일간 질문 횟수
├── avgIntentConfidence: 평균 의도 신뢰도
├── piiDetected: PII 감지 여부
├── scoreCandidate: 후보 점수
├── status: 상태 (NEW, ELIGIBLE, EXCLUDED)
├── lastAskedAt: 마지막 질문 시간
└── createdAt: 생성 시간
    │
    └── FaqDraft (FAQ 초안)
        ├── id: UUID
        ├── candidateId: 후보 ID (nullable)
        ├── domain: 도메인
        ├── question: 질문
        ├── summary: 요약
        ├── answer: 답변
        ├── status: 상태 (DRAFT, PUBLISHED, REJECTED)
        ├── reviewerId: 검토자 ID (nullable)
        ├── reviewedAt: 검토 시간 (nullable)
        └── createdAt: 생성 시간

FaqUiCategory (UI 카테고리)
├── id: UUID
├── slug: URL 슬러그 (고유)
├── displayName: 표시 이름
├── sortOrder: 정렬 순서
├── isActive: 활성 여부
└── createdAt, updatedAt, deletedAt
```

## 상태 흐름

### FAQ 생명주기

```
[수동 생성]
생성 → 활성 (isActive: true) → 비활성 (isActive: false)

[AI 자동 생성]
후보 생성 → Draft 생성 → 승인 → FAQ 생성 (활성)
              │            │
              │            └── 반려 → Draft 상태: REJECTED
              └── 실패 (PII/신뢰도 부족) → 후보 상태: EXCLUDED
```

### FaqCandidate 상태

```
NEW → ELIGIBLE → EXCLUDED
 │       │          │
 │       │          └── PII 감지 또는 신뢰도 부족
 │       └── Draft 생성 가능
 └── 새로 생성된 후보
```

### FaqDraft 상태

```
DRAFT → PUBLISHED (승인) → FAQ 생성
   │
   └── REJECTED (반려)
```

---

## 상세 플로우

### 1단계: FAQ 수동 생성

**액션**: 관리자가 "새 FAQ 만들기" 버튼 클릭

**API**: `POST /chat/faq` (chat-service)

**요청**:
```json
{
  "question": "비밀번호를 잊어버렸어요",
  "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다.",
  "domain": "SECURITY",
  "priority": 1
}
```

**결과**:
- `FAQ` 엔티티 생성
- `isActive: true` (즉시 노출 가능)
- `publishedAt` 설정
- FAQ ID 반환

**응답**:
```json
"239d0429-b517-4897-beb0-bd1f699999da"
```

---

### 2단계: FAQ 수정

**액션**: 관리자가 FAQ 내용 수정

**API**: `PATCH /chat/faq/{faqId}` (chat-service)

**요청**:
```json
{
  "question": "비밀번호를 잊어버렸어요 (수정)",
  "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다. (수정)",
  "domain": "SECURITY",
  "isActive": true,
  "priority": 2
}
```

**결과**:
- 선택적 필드 업데이트 (null인 필드는 수정하지 않음)
- `isActive` 토글 가능
- `updatedAt` 갱신

---

### 3단계: FAQ 삭제 (Soft Delete)

**액션**: 관리자가 FAQ 삭제

**API**: `DELETE /chat/faq/{faqId}` (chat-service)

**결과**:
- `isActive: false` 설정
- 실제 데이터는 유지 (소프트 삭제)
- 사용자에게 노출되지 않음

---

### 4단계: FAQ 조회 (사용자)

#### 4-1. FAQ 홈 조회

**액션**: 사용자가 FAQ 홈 화면 접근

**API**: `GET /faq/home` (chat-service)

**응답**:
```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "domain": "SECURITY",
    "question": "비밀번호를 잊어버렸어요",
    "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다.",
    "publishedAt": "2025-12-19T23:00:00Z"
  },
  {
    "id": "350d0429-b517-4897-beb0-bd1f699999db",
    "domain": "POLICY",
    "question": "휴가 신청은 어떻게 하나요?",
    "answer": "인사 시스템에서 휴가 신청 메뉴를 통해 신청하실 수 있습니다.",
    "publishedAt": "2025-12-19T23:00:00Z"
  }
]
```

**결과**:
- 도메인별로 1개씩 반환
- `isActive: true`인 FAQ만 노출
- `priority` 순으로 정렬

#### 4-2. 도메인별 FAQ 조회

**액션**: 사용자가 특정 도메인의 FAQ 목록 조회

**API**: `GET /faq?domain=SECURITY` (chat-service)

**응답**:
```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "domain": "SECURITY",
    "question": "비밀번호를 잊어버렸어요",
    "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다.",
    "publishedAt": "2025-12-19T23:00:00Z"
  }
]
```

**결과**:
- 특정 도메인의 TOP 10 FAQ
- `priority` 순으로 정렬

---

### 5단계: FAQ 조회 (관리자)

#### 5-1. 전체 FAQ 목록 조회

**API**: `GET /chat/faq` (chat-service)

**응답**:
```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "question": "비밀번호를 잊어버렸어요",
    "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다.",
    "domain": "SECURITY",
    "isActive": true,
    "priority": 1,
    "createdAt": "2025-12-19T23:00:00Z",
    "updatedAt": "2025-12-19T23:00:00Z"
  }
]
```

**결과**:
- 모든 FAQ 조회 (활성/비활성 포함)

#### 5-2. FAQ 대시보드 조회

**API**: `GET /faq/dashboard/home` (chat-service)

**결과**:
- 전체 통계 조회 (도메인별 FAQ 수 등)

**도메인별 대시보드**: `GET /faq/dashboard/SECURITY`
- 특정 도메인 통계 조회

---

### 6단계: FAQ 후보 생성

**액션**: 관리자가 FAQ 후보 생성 (또는 자동 생성)

**API**: `POST /admin/faq/candidates` (chat-service)

**요청**:
```json
{
  "question": "비밀번호를 잊어버렸어요",
  "domain": "SECURITY"
}
```

**결과**:
- `FaqCandidate` 생성
- `status: NEW`
- `piiDetected`, `avgIntentConfidence` 등 분석 결과 저장 (자동 생성 시)

**응답**:
```json
"239d0429-b517-4897-beb0-bd1f699999da"
```

---

### 7단계: FAQ 초안 자동 생성 (AI + RAGFlow)

**액션**: 관리자가 FAQ 후보에서 AI를 활용하여 초안 생성

**API**: `POST /admin/faq/candidates/{candidateId}/generate` (chat-service)

**처리 과정**:

1. **후보 검증**
   - `piiDetected: false` 확인
   - `avgIntentConfidence >= 0.7` 확인
   - 실패 시 `status: EXCLUDED` 설정 및 예외 발생

2. **AI 서버 호출**
   - `POST /ai/faq/generate` (AI Server)
   - 요청 본문:
     ```json
     {
       "domain": "POLICY",  // RAGFlow dataset으로 매핑 (SECURITY → POLICY)
       "cluster_id": "candidate-uuid",
       "canonical_question": "비밀번호를 잊어버렸어요",
       "top_docs": []  // AI 서버가 RAGFlow 직접 호출
     }
     ```

3. **AI 서버 내부 처리**
   - RAGFlow 호출: `/api/v1/retrieval` 또는 `/v1/chunk/search`
   - Milvus에서 관련 문서 청크 검색
   - LLM 호출: 컨텍스트 + 질문 → FAQ 초안 생성

4. **Draft 생성**
   - `FaqDraft` 생성
   - `status: DRAFT`
   - `question`, `summary`, `answer` 저장
   - `candidateId` 연결

**응답**:
```json
{
  "draftId": "350d0429-b517-4897-beb0-bd1f699999db"
}
```

**에러 처리**:
- RAGFlow 연결 실패: `500 Internal Server Error`
- 검색 결과 없음: `NO_DOCS_FOUND` 오류
- PII 감지 또는 신뢰도 부족: `400 Bad Request`

---

### 8단계: FAQ Draft 목록 조회

**액션**: 관리자가 FAQ 초안 목록 조회

**API**: `GET /admin/faq/drafts` (chat-service)

**Query Parameters**:
- `domain` (String, 선택): 도메인 필터
- `status` (String, 선택): 상태 필터 (DRAFT, PUBLISHED, REJECTED)

**응답**:
```json
[
  {
    "id": "350d0429-b517-4897-beb0-bd1f699999db",
    "domain": "SECURITY",
    "question": "비밀번호를 잊어버렸어요",
    "summary": "비밀번호 재설정 방법에 대한 FAQ 초안",
    "status": "DRAFT",
    "createdAt": "2025-12-19T22:00:00"
  }
]
```

---

### 9단계: FAQ Draft 승인

**액션**: 관리자가 FAQ 초안 승인하여 FAQ 생성

**API**: `POST /admin/faq/drafts/{draftId}/approve` (chat-service)

**Query Parameters**:
- `reviewerId` (UUID, 필수): 승인자 ID
- `question` (String, 필수): 승인할 질문 내용 (URL 인코딩 필요)
- `answer` (String, 필수): 승인할 답변 내용 (URL 인코딩 필요)

**처리 과정**:

1. **Draft 검증**
   - Draft 존재 확인
   - `status != PUBLISHED` 확인 (이미 승인된 Draft는 재승인 불가)
   - `status != REJECTED` 확인 (반려된 Draft는 승인 불가)

2. **FAQ 생성**
   - 새로운 `FAQ` 엔티티 생성
   - `isActive: true`
   - `publishedAt` 설정
   - `domain`은 Draft의 domain 사용

3. **Draft 상태 변경**
   - `status: PUBLISHED`
   - `reviewerId` 설정
   - `reviewedAt` 설정

4. **이력 저장**
   - `FaqRevision` 생성 (APPROVE 액션)

**응답**: `200 OK` (No Content)

**참고**:
- Query Parameter의 `question`과 `answer`는 URL 인코딩이 필요합니다.
- 한글 파라미터는 반드시 인코딩해야 합니다.

---

### 10단계: FAQ Draft 반려

**액션**: 관리자가 FAQ 초안 반려

**API**: `POST /admin/faq/drafts/{draftId}/reject` (chat-service)

**Query Parameters**:
- `reviewerId` (UUID, 필수): 반려자 ID
- `reason` (String, 필수): 반려 사유 (URL 인코딩 필요)

**처리 과정**:

1. **Draft 검증**
   - Draft 존재 확인
   - `status != PUBLISHED` 확인 (이미 승인된 Draft는 반려 불가)
   - `status != REJECTED` 확인 (이미 반려된 Draft는 재반려 불가)

2. **Draft 상태 변경**
   - `status: REJECTED`
   - `reviewerId` 설정
   - `reviewedAt` 설정

3. **이력 저장**
   - `FaqRevision` 생성 (REJECT 액션)
   - `reason` 저장

**응답**: `200 OK` (No Content)

**참고**:
- Query Parameter의 `reason`은 URL 인코딩이 필요합니다.

---

### 11단계: UI 카테고리 관리

#### 11-1. UI 카테고리 생성

**액션**: 관리자가 FAQ UI 카테고리 생성

**API**: `POST /admin/faq/ui-categories?operatorId={operatorId}` (chat-service)

**요청**:
```json
{
  "slug": "test-category-20251224105951",  // 고유 slug 필수
  "displayName": "테스트 UI 카테고리",
  "sortOrder": 1
}
```

**결과**:
- `FaqUiCategory` 생성
- `slug`는 고유해야 함 (중복 불가)
- `isActive: true`

**응답**:
```json
"239d0429-b517-4897-beb0-bd1f699999da"
```

**제약사항**:
- `slug`는 URL-safe 문자열 (소문자, 하이픈, 숫자)
- 중복 시 `400 Bad Request`: "이미 존재하는 slug 입니다."

#### 11-2. UI 카테고리 수정

**API**: `PATCH /admin/faq/ui-categories/{categoryId}?operatorId={operatorId}` (chat-service)

**요청**:
```json
{
  "slug": "security-updated",
  "displayName": "보안 (수정)",
  "sortOrder": 2
}
```

**결과**:
- `displayName`, `sortOrder` 수정 가능
- `slug` 수정 가능 (고유성 검증)

#### 11-3. UI 카테고리 비활성화

**API**: `POST /admin/faq/ui-categories/{categoryId}/deactivate?operatorId={operatorId}&reason={reason}` (chat-service)

**Query Parameters**:
- `operatorId` (UUID, 필수): 운영자 ID
- `reason` (String, 선택): 비활성화 사유 (URL 인코딩 필요)

**결과**:
- `isActive: false` 설정

#### 11-4. UI 카테고리 목록 조회

**API**: `GET /admin/faq/ui-categories` (chat-service)

**응답**:
```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "slug": "security",
    "displayName": "보안",
    "sortOrder": 1,
    "isActive": true,
    "createdAt": "2025-12-19T23:00:00Z",
    "updatedAt": "2025-12-19T23:00:00Z"
  }
]
```

---

## 통합 플로우 시나리오

### 시나리오 1: 수동 FAQ 생성 → 사용자 조회

```
1. 관리자가 FAQ 수동 생성
   POST /chat/faq
   → FAQ 생성 (isActive: true)

2. 사용자가 FAQ 홈 조회
   GET /faq/home
   → 도메인별 FAQ 반환

3. 사용자가 특정 도메인 FAQ 조회
   GET /faq?domain=SECURITY
   → TOP 10 FAQ 반환
```

---

### 시나리오 2: AI 자동 FAQ 생성 (전체 플로우)

```
1. 관리자가 FAQ 후보 생성
   POST /admin/faq/candidates
   → 후보 저장 (status: NEW)

2. 관리자가 AI로 FAQ 초안 생성
   POST /admin/faq/candidates/{candidateId}/generate
   → 후보 검증 (PII, 신뢰도)
   → AI 서버 호출 (/ai/faq/generate)
   → RAGFlow 검색 (Milvus)
   → LLM FAQ 초안 생성
   → Draft 저장 (status: DRAFT)

3. 관리자가 Draft 목록 조회
   GET /admin/faq/drafts
   → Draft 목록 반환

4. 관리자가 Draft 승인
   POST /admin/faq/drafts/{draftId}/approve
   → FAQ 생성 (isActive: true)
   → Draft 상태: PUBLISHED

5. 사용자가 FAQ 조회
   GET /faq/home
   → 승인된 FAQ 노출
```

---

### 시나리오 3: Draft 반려 후 재생성

```
1. 관리자가 Draft 반려
   POST /admin/faq/drafts/{draftId}/reject
   → Draft 상태: REJECTED

2. 관리자가 동일 후보로 Draft 재생성
   POST /admin/faq/candidates/{candidateId}/generate
   → 새로운 Draft 생성 (status: DRAFT)

3. 관리자가 수정된 Draft 승인
   POST /admin/faq/drafts/{newDraftId}/approve
   → FAQ 생성
```

---

## 에러 처리

### RAGFlow 연결 오류

**증상**:
- `NO_DOCS_FOUND` 오류
- `500 Internal Server Error`

**원인**:
- RAGFlow 서버 미실행
- 검색 결과 없음 (데이터 매칭 실패)

**해결**:
- RAGFlow 서버 실행 확인
- Mock 서버의 `retrieval_results.json`에 테스트 데이터 추가

---

### PII 감지 또는 신뢰도 부족

**증상**:
- `400 Bad Request`: "PII가 감지된 FAQ 후보는 Draft를 생성할 수 없습니다."
- `400 Bad Request`: "의도 신뢰도가 부족합니다. (현재: 0.65, 최소 요구: 0.7)"

**처리**:
- 후보 상태: `EXCLUDED`
- Draft 생성 불가

---

### UI 카테고리 Slug 중복

**증상**:
- `400 Bad Request`: "이미 존재하는 slug 입니다."

**해결**:
- 고유한 slug 생성 (예: 타임스탬프 기반)
- `test-category-{timestamp}` 형식 사용

---

### Draft 상태 오류

**증상**:
- `400 Bad Request`: "이미 승인된 FAQ 초안입니다."
- `400 Bad Request`: "반려된 FAQ 초안은 승인할 수 없습니다."

**처리**:
- 이미 승인/반려된 Draft는 재처리 불가
- 새로운 Draft 생성 필요

---

## 주요 엔드포인트 요약

### FAQ 관리

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/chat/faq` | FAQ 생성 |
| PATCH | `/chat/faq/{faqId}` | FAQ 수정 |
| DELETE | `/chat/faq/{faqId}` | FAQ 삭제 |
| GET | `/chat/faq` | FAQ 목록 조회 (관리자) |

### FAQ 조회 (사용자)

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/faq/home` | FAQ 홈 조회 |
| GET | `/faq?domain={domain}` | 도메인별 FAQ 조회 |
| GET | `/faq/dashboard/home` | 대시보드 홈 |
| GET | `/faq/dashboard/{domain}` | 도메인별 대시보드 |

### FAQ 후보 관리

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/admin/faq/candidates` | 후보 생성 |
| GET | `/admin/faq/candidates` | 후보 목록 조회 |
| POST | `/admin/faq/candidates/{candidateId}/generate` | Draft 생성 |

### FAQ Draft 관리

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/admin/faq/drafts` | Draft 목록 조회 |
| POST | `/admin/faq/drafts/{draftId}/approve` | Draft 승인 |
| POST | `/admin/faq/drafts/{draftId}/reject` | Draft 반려 |

### UI 카테고리

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/admin/faq/ui-categories` | 카테고리 생성 |
| GET | `/admin/faq/ui-categories` | 카테고리 목록 조회 |
| PATCH | `/admin/faq/ui-categories/{categoryId}` | 카테고리 수정 |
| POST | `/admin/faq/ui-categories/{categoryId}/deactivate` | 카테고리 비활성화 |

---

## 데이터 흐름 다이어그램

```
[관리자]
   │
   ├─→ [Chat Service] → [Database] (FAQ/후보/Draft 저장)
   │      │
   │      └─→ [AI Server] (FAQ 초안 생성)
   │             │
   │             ├─→ [RAGFlow] → [Milvus] (벡터 검색)
   │             │
   │             └─→ [LLM Service] (FAQ 초안 생성)
   │                    │
   │                    └─→ [Chat Service] → [Database] → [관리자]
   │
   └─→ [사용자]
          │
          └─→ [Chat Service] → [Database] (FAQ 조회)
```

---

## 참고사항

- 모든 UUID는 표준 UUID 형식입니다.
- 모든 날짜/시간은 ISO 8601 형식 (UTC)입니다.
- 인증이 필요한 API는 `Authorization: Bearer {token}` 헤더를 포함해야 합니다.
- Query Parameter에 한글이 포함된 경우 URL 인코딩이 필요합니다 (예: `question`, `answer`, `reason`).
- RAGFlow 서버가 실행 중이 아니면 Draft 생성이 실패할 수 있습니다.
- Draft 생성 시 AI 서버가 RAGFlow를 직접 호출합니다.
- `slug`는 고유해야 하며, 중복 시 오류가 발생합니다.

---

**문서 버전**: 2025-12-24  
**작성자**: AI Assistant

