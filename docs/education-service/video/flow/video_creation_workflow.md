# 영상 생성 및 게시 플로우

영상 컨텐츠 생성부터 최종 게시까지의 전체 API 플로우 문서입니다.

## 개요

영상 생성 프로세스는 다음 단계로 구성됩니다:
1. **준비 단계**: 토큰 발급 및 문서 업로드
2. **영상 컨텐츠 생성**: 교육 ID 조회 및 영상 메타 생성
3. **소스셋 생성 및 스크립트 자동 생성**: 문서 묶기 및 AI 스크립트 생성
4. **스크립트 검토 및 승인**: 스크립트 검토 요청 및 승인
5. **영상 생성**: AI 서버를 통한 영상 파일 생성
6. **영상 검토 및 게시**: 최종 검토 및 사용자 노출

---

## 1단계: 준비 (infra-service)

### 1.1 Access Token 발급

**엔드포인트**: `POST /admin/users/token/password`

**설명**: Keycloak을 통해 사용자 인증 토큰을 발급합니다.

**요청 예시**:
```json
{
  "clientId": "infra-admin",
  "clientSecret": "changeme",
  "username": "user1",
  "password": "11111",
  "scope": "openid profile email"
}
```

**응답 예시**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ...",
  "expires_in": 43200,
  "refresh_expires_in": 1800,
  "token_type": "Bearer",
  "scope": "openid profile email"
}
```

**사용**: 이후 모든 API 호출에 `Authorization: Bearer {access_token}` 헤더 사용

---

### 1.2 문서 업로드

**엔드포인트**: `POST /rag/documents/upload`

**설명**: S3에 업로드된 문서의 메타 정보를 등록합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**요청 예시**:
```json
{
  "title": "산업안전 규정집 v3",
  "domain": "HR",
  "fileUrl": "s3://ctrl-s3/docs/hr_safety_v3.pdf"
}
```

**응답 예시** (201 Created):
```json
{
  "documentId": "doc-123e4567-e89b-12d3-a456-426614174000",
  "title": "산업안전 규정집 v3",
  "domain": "HR",
  "fileUrl": "s3://ctrl-s3/docs/hr_safety_v3.pdf",
  "status": "QUEUED",
  "uploadedAt": "2025-12-24T10:00:00Z"
}
```

**중요**: `documentId`는 다음 단계에서 사용됩니다.

---

## 2단계: 영상 컨텐츠 생성 (education-service)

### 2.1 Education ID 조회

**엔드포인트**: `GET /admin/edus/with-videos`

**설명**: 모든 교육 목록과 각 교육에 포함된 영상 목록을 조회합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
```

**쿼리 파라미터** (선택):
- `status`: 영상 상태 필터 (예: `DRAFT`, `ACTIVE`)

**응답 예시**:
```json
[
  {
    "id": "edu-123e4567-e89b-12d3-a456-426614174000",
    "title": "산업안전 교육",
    "videos": [
      {
        "id": "video-123e4567-e89b-12d3-a456-426614174000",
        "title": "산업안전 교육 영상",
        "fileUrl": null,
        "duration": 0,
        "version": 1,
        "isMain": true,
        "targetDeptCode": "DEV"
      }
    ]
  }
]
```

**중요**: `educationId`는 다음 단계에서 사용됩니다.

---

### 2.2 영상 컨텐츠 생성

**엔드포인트**: `POST /admin/videos`

**설명**: DRAFT 상태의 새 교육 영상 컨텐츠를 생성합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**요청 예시**:
```json
{
  "educationId": "edu-123e4567-e89b-12d3-a456-426614174000",
  "title": "산업안전 교육 영상",
  "targetDeptCode": "DEV",
  "orderIndex": 1
}
```

**응답 예시** (201 Created):
```json
{
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "educationId": "edu-123e4567-e89b-12d3-a456-426614174000",
  "title": "산업안전 교육 영상",
  "status": "DRAFT",
  "createdAt": "2025-12-24T10:00:00Z"
}
```

**중요**: `videoId`는 이후 모든 단계에서 사용됩니다.

**상태**: `DRAFT` → (스크립트 생성 완료 후) `SCRIPT_READY`

---

## 3단계: 소스셋 생성 및 스크립트 자동 생성 (education-service)

### 3.1 소스셋 생성

**엔드포인트**: `POST /video/source-sets`

**설명**: 여러 문서를 하나의 영상 제작 단위(SourceSet)로 묶고, AI 서버에 스크립트 생성 작업을 자동으로 요청합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**요청 예시**:
```json
{
  "title": "산업안전 교육 소스셋",
  "domain": "policy",
  "educationId": "edu-123e4567-e89b-12d3-a456-426614174000",
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "documentIds": [
    "doc-123e4567-e89b-12d3-a456-426614174000"
  ]
}
```

**응답 예시** (201 Created):
```json
{
  "sourceSetId": "source-set-123e4567-e89b-12d3-a456-426614174000",
  "title": "산업안전 교육 소스셋",
  "domain": "policy",
  "educationId": "edu-123e4567-e89b-12d3-a456-426614174000",
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "documentIds": [
    "doc-123e4567-e89b-12d3-a456-426614174000"
  ],
  "status": "PROCESSING",
  "createdAt": "2025-12-24T10:00:00Z"
}
```

**백엔드 동작**:
1. SourceSet을 DB에 저장
2. 트랜잭션 커밋 후 AI 서버에 `/v2.1/source-sets/{sourceSetId}/start` 호출
3. AI 서버가 백그라운드에서 다음 작업 수행:
   - RAGFlow에서 문서 다운로드 및 처리
   - 문서 청크 추출
   - LLM을 통한 스크립트 생성

**중요**: 이 단계는 비동기로 처리되며, 스크립트 생성 완료까지 시간이 걸릴 수 있습니다.

---

### 3.2 스크립트 생성 완료 대기

**설명**: AI 서버가 스크립트 생성 완료까지 대기합니다. (명시적 API 호출 없음)

**대기 시간**: 문서 크기와 복잡도에 따라 수분~수십 분 소요될 수 있습니다.

**확인 방법**: 다음 단계의 `GET /scripts/lookup` API로 스크립트 생성 여부를 확인할 수 있습니다.

---

### 3.3 스크립트 생성 완료 콜백 (AI → 백엔드)

**엔드포인트**: `POST /internal/callbacks/source-sets/{sourceSetId}/complete`

**설명**: AI 서버가 스크립트 생성 완료 후 백엔드로 결과를 전달합니다. (내부 API)

**요청 헤더**:
```
X-Internal-Token: {internal_token}
Content-Type: application/json
```

**요청 예시**:
```json
{
  "status": "SUCCESS",
  "script": {
    "title": "직장내괴롭힘 교육 영상",
    "total_duration_sec": 720,
    "chapters": [
      {
        "title": "괴롭힘",
        "duration_sec": 180,
        "scenes": [
          {
            "scene_id": 1,
            "purpose": "hook",
            "visual": "자료 원문 문장(텍스트) 강조",
            "narration": "직장 내 괴롭힘이란...",
            "caption": "직장 내 괴롭힘이란...",
            "duration_sec": 15,
            "source_chunks": [1, 2, 3]
          }
        ]
      }
    ]
  },
  "version": 1
}
```

**백엔드 동작**:
1. 스크립트를 DB에 저장
2. 영상 상태를 `SCRIPT_READY`로 변경
3. SourceSet 상태를 `COMPLETED`로 변경

**에러 처리**: 
- SourceSet이 이미 삭제된 경우 404 응답 (정상 처리, 경고 로그만 기록)
- 스크립트 생성 실패 시 `status: "FAILED"`와 함께 에러 정보 전달

---

## 4단계: 스크립트 검토 및 승인 (education-service)

### 4.1 스크립트 생성 확인

**엔드포인트**: `GET /scripts/lookup?videoId={videoId}`

**설명**: `videoId` 또는 `educationId`로 스크립트 ID를 조회합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
```

**쿼리 파라미터**:
- `videoId` (선택): 영상 ID
- `educationId` (선택): 교육 ID (둘 다 제공된 경우 `videoId` 우선)

**응답 예시**:
```json
{
  "scriptId": "script-123e4567-e89b-12d3-a456-426614174000",
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "educationId": "edu-123e4567-e89b-12d3-a456-426614174000",
  "status": "SCRIPT_READY"
}
```

**중요**: `scriptId`는 다음 단계에서 사용됩니다.

---

### 4.2 스크립트 검토 요청 (1차)

**엔드포인트**: `PUT /admin/videos/{videoId}/review-request`

**설명**: 스크립트를 검토자에게 검토 요청합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
```

**경로 파라미터**:
- `videoId`: 영상 ID

**상태 변경**: `SCRIPT_READY` → `SCRIPT_REVIEW_REQUESTED`

**응답 예시**:
```json
{
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "status": "SCRIPT_REVIEW_REQUESTED",
  "updatedAt": "2025-12-24T10:30:00Z"
}
```

---

### 4.3 스크립트 1차 승인

**엔드포인트**: `POST /scripts/{scriptId}/approve`

**설명**: 검토자가 스크립트를 승인합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
```

**경로 파라미터**:
- `scriptId`: 스크립트 ID

**상태 변경**: `SCRIPT_REVIEW_REQUESTED` → `SCRIPT_APPROVED`

**응답 예시**:
```json
{
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "status": "SCRIPT_APPROVED",
  "updatedAt": "2025-12-24T10:35:00Z"
}
```

**참고**: 스크립트 반려는 `POST /scripts/{scriptId}/reject` 엔드포인트를 사용합니다.

---

## 5단계: 영상 생성 (education-service)

### 5.1 영상 생성 요청

**엔드포인트**: `POST /video/job`

**설명**: 최종 확정된 스크립트를 기반으로 영상 생성 Job을 등록합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**요청 예시**:
```json
{
  "scriptId": "script-123e4567-e89b-12d3-a456-426614174000",
  "educationId": "edu-123e4567-e89b-12d3-a456-426614174000",
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000"
}
```

**응답 예시** (201 Created):
```json
{
  "jobId": "job-123e4567-e89b-12d3-a456-426614174000",
  "scriptId": "script-123e4567-e89b-12d3-a456-426614174000",
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "status": "PENDING",
  "createdAt": "2025-12-24T11:00:00Z"
}
```

**백엔드 동작**:
1. Job을 DB에 저장
2. AI 서버에 영상 생성 작업 요청
3. Job 상태를 `PROCESSING`으로 변경
4. AI 서버가 백그라운드에서 영상 생성 수행

**중요**: 이 단계는 비동기로 처리되며, 영상 생성 완료까지 시간이 걸릴 수 있습니다.

---

### 5.2 영상 생성 완료 대기

**설명**: AI 서버가 영상 생성 완료까지 대기합니다. (명시적 API 호출 없음)

**대기 시간**: 스크립트 길이와 복잡도에 따라 수십 분~수시간 소요될 수 있습니다.

**확인 방법**: `GET /video/job/{jobId}` API로 Job 상태를 확인할 수 있습니다.

---

### 5.3 영상 생성 완료 콜백 (AI → 백엔드)

**엔드포인트**: `POST /video/job/{jobId}/complete`

**설명**: AI 서버가 영상 생성 완료 후 백엔드로 결과를 전달합니다. (내부 API)

**요청 헤더**:
```
X-Internal-Token: {internal_token}
Content-Type: application/json
```

**요청 예시**:
```json
{
  "status": "SUCCESS",
  "videoFileUrl": "s3://ctrl-s3/videos/video-123e4567-e89b-12d3-a456-426614174000.mp4",
  "duration": 720,
  "thumbnailUrl": "s3://ctrl-s3/thumbnails/video-123e4567-e89b-12d3-a456-426614174000.jpg",
  "metadata": {
    "width": 1920,
    "height": 1080,
    "fps": 30,
    "codec": "h264"
  }
}
```

**백엔드 동작**:
1. 영상 파일 URL을 영상 메타에 저장
2. Job 상태를 `COMPLETED`로 변경
3. 영상 상태를 `READY`로 변경

**에러 처리**: 
- Job이 존재하지 않는 경우 404 응답
- 영상 생성 실패 시 `status: "FAILED"`와 함께 에러 정보 전달

---

## 6단계: 영상 검토 및 게시 (education-service)

### 6.1 영상 검토 요청 (2차)

**엔드포인트**: `PUT /admin/videos/{videoId}/review-request`

**설명**: 생성된 영상을 검토자에게 검토 요청합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
```

**경로 파라미터**:
- `videoId`: 영상 ID

**상태 변경**: `READY` → `REVIEW_REQUESTED`

**응답 예시**:
```json
{
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "status": "REVIEW_REQUESTED",
  "updatedAt": "2025-12-24T12:00:00Z"
}
```

---

### 6.2 영상 검토 승인

**엔드포인트**: `PUT /admin/videos/{videoId}/approve`

**설명**: 검토자가 영상을 승인합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
```

**경로 파라미터**:
- `videoId`: 영상 ID

**상태 변경**: `REVIEW_REQUESTED` → `APPROVED`

**응답 예시**:
```json
{
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "status": "APPROVED",
  "updatedAt": "2025-12-24T12:30:00Z"
}
```

**참고**: 영상 반려는 `PUT /admin/videos/{videoId}/reject` 엔드포인트를 사용합니다.

---

### 6.3 영상 게시

**엔드포인트**: `PUT /admin/videos/{videoId}/publish`

**설명**: 승인된 영상을 사용자에게 노출합니다.

**요청 헤더**:
```
Authorization: Bearer {access_token}
```

**경로 파라미터**:
- `videoId`: 영상 ID

**상태 변경**: `APPROVED` → `ACTIVE`

**응답 예시**:
```json
{
  "videoId": "video-123e4567-e89b-12d3-a456-426614174000",
  "status": "ACTIVE",
  "updatedAt": "2025-12-24T13:00:00Z"
}
```

**완료**: 이제 영상이 사용자에게 노출됩니다.

---

## 전체 상태 전이도

```
영상 상태:
DRAFT 
  → SCRIPT_READY (스크립트 생성 완료)
  → SCRIPT_REVIEW_REQUESTED (스크립트 검토 요청)
  → SCRIPT_APPROVED (스크립트 승인)
  → READY (영상 생성 완료)
  → REVIEW_REQUESTED (영상 검토 요청)
  → APPROVED (영상 승인)
  → ACTIVE (게시)
```

---

## 에러 처리

### 일반적인 에러 응답

**400 Bad Request**: 잘못된 요청 파라미터
```json
{
  "error": "Invalid request",
  "message": "videoId is required"
}
```

**401 Unauthorized**: 인증 실패
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

**404 Not Found**: 리소스를 찾을 수 없음
```json
{
  "error": "Not Found",
  "message": "Video not found: {videoId}"
}
```

**409 Conflict**: 상태 변경 불가
```json
{
  "error": "Conflict",
  "message": "Cannot change status from {current} to {target}"
}
```

---

## 주의사항

1. **비동기 처리**: 스크립트 생성(3단계)과 영상 생성(5단계)은 비동기로 처리되며, 완료까지 시간이 걸릴 수 있습니다.
2. **상태 확인**: 각 단계에서 상태를 확인하고 다음 단계로 진행하세요.
3. **에러 복구**: 실패한 작업은 재시도할 수 있습니다 (예: `POST /video/job/{jobId}/retry`).
4. **권한**: 모든 API는 `ROLE_ADMIN` 권한이 필요합니다.
5. **내부 API**: 콜백 엔드포인트(`/internal/*`)는 내부 토큰(`X-Internal-Token`)이 필요하며, 외부에서 직접 호출하지 않습니다.

---

## 관련 문서

- [SourceSet API 명세](./source-set/api/source_set_api_spec.md)
- [Video API 명세](./video/api/video_api_spec.md)
- [Script API 명세](./script/api/script_api_spec.md)
- [RAG Documents API 명세](../../infra-service/rag/api/rag_documents_api_spec.md)

