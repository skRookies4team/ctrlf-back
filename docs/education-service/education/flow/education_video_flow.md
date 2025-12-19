# 교육 영상(컨텐츠) 생성 플로우

## 개요

교육 영상 컨텐츠는 `EducationVideo` 엔티티를 중심으로 관리됩니다.
교육(Education)은 5개 카테고리로 고정되어 있으며, 각 교육에 여러 영상 컨텐츠를 생성할 수 있습니다.

## 엔티티 구조

```
Education (5개 고정)
├── 직무 (JOB_DUTY, edu_type: JOB)
├── 성희롱 예방 (SEXUAL_HARASSMENT, edu_type: MANDATORY)
├── 개인정보 보호 (PRIVACY_PROTECTION, edu_type: MANDATORY)
├── 직장 내 괴롭힘 (WORKPLACE_HARASSMENT, edu_type: MANDATORY)
└── 장애인 인식 개선 (DISABILITY_AWARENESS, edu_type: MANDATORY)
    │
    └── EducationVideo (교육 컨텐츠)
        ├── id: UUID
        ├── educationId: 소속 교육 ID
        ├── title: 영상 제목
        ├── scriptId: 연결된 스크립트 ID ★ NEW
        ├── materialId: 연결된 자료(RagDocument) ID ★ NEW
        ├── status: 상태 (아래 참조)
        ├── departmentScope: 수강 가능 부서 (JSON, null=전체)
        ├── fileUrl: 생성된 영상 URL
        ├── duration: 영상 길이(초)
        └── generationJobId: 영상 생성 Job ID
            │
            └── VideoGenerationJob (영상 생성 작업)
                ├── scriptId: 스크립트 ID
                ├── status: 작업 상태
                └── ...
```

## 상태 흐름

### EducationVideo 상태 (검토 2회)

```
DRAFT → SCRIPT_GENERATING → SCRIPT_READY → SCRIPT_REVIEW_REQUESTED → SCRIPT_APPROVED → PROCESSING → READY → FINAL_REVIEW_REQUESTED → PUBLISHED
  │            │                  │                   │                     │               │          │               │                  │
  │            │                  │                   │                     │               │          │               │                  └── 유저에게 노출
  │            │                  │                   │                     │               │          │               └── 2차 검토 요청 (영상)
  │            │                  │                   │                     │               │          └── 영상 생성 완료
  │            │                  │                   │                     │               └── AI 영상 생성 중
  │            │                  │                   │                     └── 1차 승인 (영상 생성 가능)
  │            │                  │                   └── 1차 검토 요청 (스크립트)
  │            │                  └── 스크립트 생성 완료
  │            └── AI 스크립트 생성 중
  └── 초기 생성
```

### 검토 흐름 요약

```
[1차 검토 - 스크립트]
SCRIPT_READY → review-request → SCRIPT_REVIEW_REQUESTED → approve → SCRIPT_APPROVED
                                                         → reject → SCRIPT_READY (수정 후 재요청)

[2차 검토 - 영상]
READY → review-request → FINAL_REVIEW_REQUESTED → approve → PUBLISHED (유저 노출)
                                                 → reject → READY (수정 후 재요청)
```

### RagDocument 상태

```
QUEUED → PROCESSING → COMPLETED
    │         │           │
    │         │           └── 임베딩 완료 (스크립트 생성 가능)
    │         └── AI 임베딩 처리 중
    └── 업로드 완료 대기
```

## 상세 플로우

### 1단계: 새 교육 영상 만들기

**액션**: 관리자가 "새 교육 영상 만들기" 버튼 클릭

**API**: `POST /admin/videos` (education-service)

**요청**:

```json
{
  "educationId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "2024년 성희롱 예방 교육"
}
```

**결과**:

- `EducationVideo` 생성 (`status: DRAFT`)
- 영상 URL은 아직 없음 (null)

---

### 2단계: 자료 업로드 (S3)

**액션**: 폼에서 파일 선택 후 S3에 업로드

**API 1**: `POST /infra/files/presign/upload` (infra-service)

**요청**:

```json
{
  "type": "docs",
  "filename": "2024-harassment-prevention.pdf",
  "contentType": "application/pdf"
}
```

**응답**:

```json
{
  "presignedUrl": "https://s3.amazonaws.com/bucket/...",
  "fileUrl": "s3://bucket/docs/2024-harassment-prevention.pdf"
}
```

---

### 3단계: 자료 메타 등록 + 임베딩

**액션**: S3 업로드 완료 후 메타 정보 저장 + AI 서버에 전처리/임베딩 요청

**API**: `POST /rag/documents/upload` (infra-service)

**요청**:

```json
{
  "title": "2024년 성희롱 예방 교육 자료",
  "domain": "HR",
  "uploaderUuid": "c13c91f2-fb1a-4d42-b381-72847a52fb99",
  "fileUrl": "s3://bucket/docs/2024-harassment-prevention.pdf"
}
```

**응답**:

```json
{
  "documentId": "c4d58288-5c7e-4e75-a11c-77f83ffbb64b",
  "status": "QUEUED",
  "createdAt": "2025-12-18T07:31:35.290642Z"
}
```

**결과**:

- `RagDocument` 생성
- `documentId` 획득 (= `materialId`)
- **AI 서버에 전처리/임베딩 요청** (`ragAiClient.process()`)

---

### 3.5단계: 임베딩 상태 확인 (폴링)

**액션**: 프론트엔드가 임베딩 완료를 폴링

**API**: `GET /rag/documents/{documentId}/status` (infra-service) ★ NEW

**응답**:

```json
{
  "documentId": "550e8400-e29b-41d4-a716-446655440002",
  "status": "COMPLETED",
  "createdAt": "2024-01-01T10:00:00Z",
  "processedAt": "2024-01-01T10:05:00Z"
}
```

**결과**:

- `status: COMPLETED` 확인 시 → 스크립트 생성 가능

---

### 4단계: 스크립트 자동생성

**액션**: "스크립트 생성" 버튼 클릭 (임베딩 완료 후)

**API**: `POST /script/generate/{materialId}` (education-service)

**요청**:

```json
{
  "eduId": "550e8400-e29b-41d4-a716-446655440000",
  "videoId": "550e8400-e29b-41d4-a716-446655440003",
  "fileUrl": "s3://bucket/docs/2024-harassment-prevention.pdf"
}
```

- `EducationVideo.materialId` 연결
- `EducationVideo.status` → `SCRIPT_GENERATING`
- **AI 서버에 스크립트 생성 요청** (임베딩된 데이터 기반)

---

### 4.5단계: 스크립트 생성 완료 (콜백)

**액션**: AI 서버가 스크립트 생성 완료 후 콜백

**API**: `POST /script/complete` (AI 서버 → education-service)

**콜백 데이터**:

```json
{
  "videoId": "550e8400-e29b-41d4-a716-446655440003",
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

> **Note**: `script` 필드는 JSON 객체로 전송되며, 백엔드에서 JSON 문자열로 변환하여 `rawPayload`에 저장됩니다.

**결과**:

- `EducationScript` 신규 생성 (ID 자동 부여)
- `EducationVideo.scriptId` 연결
- `EducationVideo.status` → `SCRIPT_READY`
- 생성된 스크립트를 폼에서 확인/수정 가능

**스크립트 재생성 시 (반려 후 재생성)**:

- 기존 스크립트에 `deleted_at` 설정 (소프트 삭제)
- 새 스크립트 생성 시 `version` 자동 증가 (기존 버전 + 1)
- `EducationVideo.scriptId`는 새 스크립트로 업데이트
- 이전 버전 스크립트는 히스토리로 보관 (감사/디버깅용)

---

### 5단계: 1차 검토 요청 (스크립트)

**액션**: 스크립트 생성 완료 후 "검토 요청" 버튼 클릭

**API**: `PUT /admin/videos/{videoId}/review-request` (education-service)

**결과**:

- `EducationVideo.status` → `SCRIPT_REVIEW_REQUESTED`
- 검토자(CONTENTS_REVIEWER)에게 스크립트 검토 요청

---

### 5.5단계: 1차 승인/반려 (스크립트)

**액션**: 검토자가 스크립트 검토 후 승인 또는 반려

**API**: `PUT /admin/videos/{videoId}/approve` 또는 `/reject`

**승인 시**:

- `EducationVideo.status` → `SCRIPT_APPROVED`
- **이제 영상 생성이 가능해집니다**

**반려 시**:

- `EducationVideo.status` → `SCRIPT_READY`
- 스크립트 수정 후 재요청

---

### 6단계: 영상 자동생성

**액션**: 1차 승인 후 "영상 생성" 버튼 클릭

**API**: `POST /video/job` (education-service)

**요청**:

```json
{
  "eduId": "550e8400-e29b-41d4-a716-446655440000",
  "scriptId": "550e8400-e29b-41d4-a716-446655440001",
  "videoId": "550e8400-e29b-41d4-a716-446655440003"
}
```

**조건**: `SCRIPT_APPROVED` 상태에서만 호출 가능

**결과**:

- `VideoGenerationJob` 생성 (`status: QUEUED`)
- `EducationVideo.status` → `PROCESSING`
- AI 서버에서 영상 생성 시작

---

### 6.5단계: 영상 생성 완료 (콜백)

**액션**: AI 서버가 영상 생성 완료 후 콜백

**API**: `POST /video/job/{jobId}/complete` (AI 서버 → education-service)

**콜백 데이터**:

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "videoUrl": "https://cdn.example.com/videos/2024-harassment.mp4",
  "duration": 1200,
  "status": "COMPLETED"
}
```

**결과**:

- `EducationVideo.fileUrl` 업데이트
- `EducationVideo.duration` 업데이트
- `EducationVideo.status` → `READY`

---

### 7단계: 영상 생성 상태 확인 (폴링)

**액션**: 프론트엔드가 영상 생성 완료를 폴링

**API**: `GET /admin/videos/{videoId}` (education-service)

**응답**:

```json
{
  "videoId": "550e8400-e29b-41d4-a716-446655440003",
  "educationId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "2024년 성희롱 예방 교육",
  "status": "READY",
  "fileUrl": "https://cdn.example.com/videos/2024-harassment.mp4",
  "duration": 1200,
  "departmentScope": null,
  "scriptId": "550e8400-e29b-41d4-a716-446655440001",
  "materialId": "550e8400-e29b-41d4-a716-446655440002"
}
```

**폴링 로직**:

- `status: PROCESSING` → 영상 생성 중 (계속 폴링)
- `status: READY` → 영상 생성 완료! (2차 검토 요청 가능)
- 권장 폴링 간격: 3~5초

---

### 8단계: 2차 검토 요청 (영상)

**액션**: 관리자가 "최종 검토 요청" 버튼 클릭

**API**: `PUT /admin/videos/{videoId}/review-request` (education-service)

**결과**:

- `EducationVideo.status` → `FINAL_REVIEW_REQUESTED`
- 검토자에게 최종 검토 요청 (스크립트 + 영상)

---

### 9단계: 2차 승인/반려 (최종)

**액션**: 검토자가 스크립트 + 영상 최종 검토 후 승인 또는 반려

**API**: `PUT /admin/videos/{videoId}/approve` 또는 `/reject` (education-service)

**승인 시**:

- `EducationVideo.status` → `PUBLISHED`
- **유저에게 즉시 노출됨** (별도 게시 단계 없음)

**반려 시**:

- `EducationVideo.status` → `READY`
- 영상 수정 후 2차 검토 재요청

---

## API 요약

| 단계 | 액션                     | 서비스            | API                                           | 상태    |
| ---- | ------------------------ | ----------------- | --------------------------------------------- | ------- |
| 1    | 영상 컨텐츠 생성         | education-service | `POST /admin/videos`                          | ✅ 구현 |
| 2    | S3 업로드 URL 발급       | infra-service     | `POST /infra/files/presign/upload`            | ✅ 구현 |
| 3    | 자료 메타 등록           | infra-service     | `POST /rag/documents/upload`                  | ✅ 구현 |
| 3.5  | 임베딩 상태 확인         | infra-service     | `GET /rag/documents/{id}/status`              | ✅ 구현 |
| 4    | 스크립트 자동생성        | education-service | `POST /script/generate/{materialId}`          | ✅ 구현 |
| 4.5  | 스크립트 완료 콜백       | education-service | `POST /script/complete`                       | ✅ 구현 |
| 5    | 1차 검토 요청 (스크립트) | education-service | `PUT /admin/videos/{videoId}/review-request`  | ✅ 구현 |
| 5.5  | 1차 승인/반려            | education-service | `PUT /admin/videos/{videoId}/approve\|reject` | ✅ 구현 |
| 6    | 영상 자동생성            | education-service | `POST /video/job`                             | ✅ 구현 |
| 6.5  | 영상 완료 콜백           | education-service | `POST /video/job/{jobId}/complete`            | ✅ 구현 |
| 7    | 영상 상태 확인 (폴링)    | education-service | `GET /admin/videos/{videoId}`                 | ✅ 구현 |
| 8    | 2차 검토 요청 (영상)     | education-service | `PUT /admin/videos/{videoId}/review-request`  | ✅ 구현 |
| 9    | 2차 승인 (게시) / 반려   | education-service | `PUT /admin/videos/{videoId}/approve\|reject` | ✅ 구현 |
| -    | 상태 강제 변경 (개발용)  | education-service | `PUT /admin/videos/{videoId}/status`          | ✅ 구현 |

> **Note**: `review-request`, `approve`, `reject` API는 현재 상태에 따라 자동으로 1차/2차 검토로 분기됩니다.

## 열람 권한 (departmentScope)

- `departmentScope`가 `null` 또는 빈 배열 → **전체 부서 접근 가능**
- `departmentScope`가 `["HR", "IT"]` → **HR, IT 부서만 접근 가능**

```json
// 전체 부서 접근
{ "departmentScope": null }

// 특정 부서만 접근
{ "departmentScope": "[\"HR\", \"IT\", \"Finance\"]" }
```

## 프론트엔드 UI 흐름

```
┌─────────────────────────────────────────────────────────┐
│  교육 영상 관리                                          │
├─────────────────────────────────────────────────────────┤
│  [+ 새 교육 영상 만들기]                                 │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 제목: [2024년 성희롱 예방 교육        ]         │   │
│  │ 카테고리: [성희롱 예방 ▼]                       │   │
│  │ 열람 부서: [전체 ▼]                             │   │
│  │                                                  │   │
│  │ 자료 업로드: [파일 선택]                        │   │
│  │                                                  │   │
│  │ [스크립트 생성]  ← 2단계                        │   │
│  │                                                  │   │
│  │ 스크립트:                                       │   │
│  │ ┌──────────────────────────────────────────┐   │   │
│  │ │ (AI 생성 스크립트, 편집 가능)            │   │   │
│  │ └──────────────────────────────────────────┘   │   │
│  │                                                  │   │
│  │ [영상 생성]  ← 3단계                            │   │
│  │                                                  │   │
│  │ 영상 미리보기:                                  │   │
│  │ ┌──────────────────────────────────────────┐   │   │
│  │ │ [▶️ 생성된 영상 미리보기]                │   │   │
│  │ └──────────────────────────────────────────┘   │   │
│  │                                                  │   │
│  │ [검토 요청]  ← 5단계                            │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 구현 완료 사항

### infra-service

- ✅ **S3 Presigned URL 발급**: `POST /infra/files/presign/upload`
- ✅ **자료 메타 등록 + 임베딩 요청**: `POST /rag/documents/upload`
- ✅ **임베딩 상태 조회**: `GET /rag/documents/{id}/status` ★ NEW

### education-service

- ✅ **DRAFT 상태 영상 생성 API**: `POST /admin/videos`
- ✅ **스크립트 자동생성 요청**: `POST /script/generate/{materialId}` (videoId 연결 포함)
- ✅ **스크립트 생성 완료 콜백**: `POST /script/complete` (EducationVideo.scriptId 연결, 재생성 시 버전 관리)
- ✅ **영상 자동생성 요청**: `POST /video/job` (SCRIPT_APPROVED 상태에서만 가능)
- ✅ **영상 생성 완료 콜백**: `POST /video/job/{jobId}/complete`
- ✅ **영상 상세 조회**: `GET /admin/videos/{videoId}`
- ✅ **영상 목록 조회**: `GET /admin/videos/list`
- ✅ **검토 요청 API**: `PUT /admin/videos/{videoId}/review-request` (1차/2차 자동 분기)
- ✅ **승인 API**: `PUT /admin/videos/{videoId}/approve` (1차/2차 자동 분기)
- ✅ **반려 API**: `PUT /admin/videos/{videoId}/reject` (1차/2차 자동 분기)
- ✅ **상태 강제 변경 (개발용)**: `PUT /admin/videos/{videoId}/status?status={STATUS}`

## 엔티티 연결 관계

```
EducationVideo
├── educationId → Education.id
├── scriptId → EducationScript.id ★ NEW
├── materialId → RagDocument.id (infra-service) ★ NEW
└── generationJobId → VideoGenerationJob.id
```

## 개발용 API

### 상태 강제 변경 API

**API**: `PUT /admin/videos/{videoId}/status?status={STATUS}`

**용도**: 개발/테스트 시 영상 상태를 강제로 변경

**사용 가능한 상태**:

- `DRAFT` - 초기 생성
- `SCRIPT_GENERATING` - 스크립트 생성 중
- `SCRIPT_READY` - 스크립트 생성 완료
- `SCRIPT_REVIEW_REQUESTED` - 1차 검토 요청 (스크립트)
- `SCRIPT_APPROVED` - 1차 승인 (영상 생성 가능)
- `PROCESSING` - 영상 생성 중
- `READY` - 영상 생성 완료
- `FINAL_REVIEW_REQUESTED` - 2차 검토 요청 (영상)
- `PUBLISHED` - 최종 승인/게시 (유저 노출)

> ⚠️ **주의**: 상태 검증 없이 강제 변경되므로 테스트 용도로만 사용하세요.

---

## 추가 구현 필요 사항

1. **검토 요청 시 알림 발송** (선택)
2. **스크립트 히스토리 조회 API** (선택) - 이전 버전 스크립트 목록 조회
