# 교육 영상 생성 테스트 시나리오

## 테스트 환경

```
education-service: http://localhost:9002
infra-service: http://localhost:9003
```

## 사전 준비

### 1. JWT 토큰 발급

Keycloak에서 토큰을 발급받아 환경변수로 설정합니다.

```bash
# 토큰 발급 (예시)
export TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6..."
```

### 2. educationId 확인

기존 Education 목록에서 educationId를 확인합니다.

```bash
curl -X GET 'http://localhost:9002/admin/edus/with-videos' \
  -H 'Authorization: Bearer '$TOKEN
```

응답에서 `educationId`를 확인하여 환경변수로 설정:

```bash
export EDUCATION_ID="실제_교육_ID"
```

---

## 전체 테스트 시나리오

### 📌 Step 1: 영상 컨텐츠 생성 (DRAFT)

```bash
curl -X POST 'http://localhost:9002/admin/videos' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "educationId": "'$EDUCATION_ID'",
    "title": "2024년 직장 내 괴롭힘 예방 교육",
    "departmentScope": null
  }'
```

**응답 예시:**

```json
{
  "videoId": "5e68abb2-98d3-4fcd-92aa-e398e7129946",
  "status": "DRAFT"
}
```

**환경변수 설정:**

```bash
export VIDEO_ID="응답에서_받은_videoId"
```

**✅ 확인사항:**

- `status`가 `DRAFT`인지 확인
- `videoId`를 저장

**⚠️ 주의사항:**

- `educationId`가 존재하지 않으면 FK 에러 발생
- `GET /admin/edus/with-videos`로 유효한 educationId 확인 필요

---

### 📌 Step 2: S3 업로드 URL 발급

```bash
curl -X POST 'http://localhost:9003/infra/files/presign/upload' \
  -H 'Content-Type: application/json' \
  -d '{
    "filename": "workplace-harassment-2024.pdf",
    "contentType": "application/pdf",
    "type": "docs"
  }'
```

**응답 예시:**

```json
{
  "uploadUrl": "https://s3.ap-northeast-2.amazonaws.com/bucket/docs/uuid-workplace-harassment-2024.pdf?X-Amz-...",
  "fileUrl": "s3://bucket/docs/uuid-workplace-harassment-2024.pdf"
}
```

**환경변수 설정:**

```bash
export UPLOAD_URL="응답에서_받은_uploadUrl"
export FILE_URL="응답에서_받은_fileUrl"
```

**✅ 확인사항:**

- `uploadUrl`과 `fileUrl`을 모두 저장
- `type`은 `docs`, `image`, `video` 중 하나

---

### 📌 Step 2.5: S3에 파일 업로드

```bash
curl -X PUT '$UPLOAD_URL' \
  -H 'Content-Type: application/pdf' \
  --data-binary '@/path/to/your/file.pdf'
```

**✅ 확인사항:**

- HTTP 200 응답 확인
- Content-Type이 요청 시 지정한 것과 일치해야 함

**⚠️ 주의사항:**

- Presigned URL은 유효 시간이 있음 (기본 15분)
- 파일 크기 제한 확인 필요

---

### 📌 Step 3: 자료 메타 등록 + 임베딩 요청

```bash
curl -X POST 'http://localhost:9003/rag/documents/upload' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "2024년 직장 내 괴롭힘 예방 교육 자료",
    "domain": "HR",
    "uploaderUuid": "c13c91f2-fb1a-4d42-b381-72847a52fb99",
    "fileUrl": "'$FILE_URL'"
  }'
```

**응답 예시:**

```json
{
  "documentId": "a5b376e7-464b-47e5-86fd-fe5697e0e614",
  "status": "QUEUED",
  "createdAt": "2025-12-18T09:44:00.000Z"
}
```

**환경변수 설정:**

```bash
export MATERIAL_ID="응답에서_받은_documentId"
```

**✅ 확인사항:**

- `status`가 `QUEUED`인지 확인
- `documentId`를 `MATERIAL_ID`로 저장 (= materialId)

---

### 📌 Step 3.5: 임베딩 상태 확인 (폴링)

```bash
curl -X GET 'http://localhost:9003/rag/documents/'$MATERIAL_ID'/status' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "documentId": "a5b376e7-464b-47e5-86fd-fe5697e0e614",
  "status": "COMPLETED",
  "createdAt": "2025-12-18T09:44:00.000Z",
  "processedAt": "2025-12-18T09:45:00.000Z"
}
```

**✅ 확인사항:**

- `status`가 `COMPLETED`가 될 때까지 폴링
- `QUEUED` → `PROCESSING` → `COMPLETED`

**⚠️ 현재 상태 (MOCK):**

- AI 서버 미연동 시 상태가 `QUEUED`에서 변하지 않을 수 있음
- 개발 테스트 시에는 상태 확인 스킵하고 다음 단계 진행

---

### 📌 Step 4: 스크립트 자동생성 요청

```bash
curl -X POST 'http://localhost:9002/script/generate/'$MATERIAL_ID \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "eduId": "'$EDUCATION_ID'",
    "videoId": "'$VIDEO_ID'",
    "fileUrl": "'$FILE_URL'"
  }'
```

**응답 예시:**

```json
{
  "received": true,
  "status": "SCRIPT_GENERATING"
}
```

**✅ 확인사항:**

- `status`가 `SCRIPT_GENERATING`인지 확인
- `EducationVideo.materialId`가 연결됨

---

### 📌 Step 4.5: 스크립트 생성 완료 콜백 (AI 서버 → 백엔드)

실제로는 AI 서버가 호출하지만, 테스트 시에는 직접 호출:

```bash
curl -X POST 'http://localhost:9002/script/complete' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "videoId": "'$VIDEO_ID'",
    "script": {
      "title": "직장 내 괴롭힘 예방 교육",
      "total_duration_sec": 720,
      "chapters": [
        {
          "title": "직장 내 괴롭힘이란",
          "duration_sec": 180,
          "scenes": [
            {
              "scene_id": 1,
              "purpose": "hook",
              "visual": "자료 원문 문장(텍스트) 강조",
              "narration": "직장 내 괴롭힘이란 사업장에서 지위 또는 관계의 우위를 이용하여...",
              "caption": "직장 내 괴롭힘이란?",
              "duration_sec": 30,
              "source_chunks": [1, 2, 3]
            }
          ]
        }
      ]
    },
    "version": 1
  }'
```

**응답 예시:**

```json
{
  "saved": true,
  "scriptId": "932e7e68-2d7a-41ae-87b1-67e570aedd24"
}
```

**환경변수 설정:**

```bash
export SCRIPT_ID="응답에서_받은_scriptId"
```

**✅ 확인사항:**

- `saved`가 `true`인지 확인
- `scriptId`를 저장

---

### 📌 Step 5: 1차 검토 요청 (스크립트)

```bash
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/review-request' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "videoId": "5e68abb2-98d3-4fcd-92aa-e398e7129946",
  "previousStatus": "SCRIPT_READY",
  "currentStatus": "SCRIPT_REVIEW_REQUESTED",
  "updatedAt": "2025-12-18T10:00:00.000Z"
}
```

**✅ 확인사항:**

- `previousStatus`가 `SCRIPT_READY`
- `currentStatus`가 `SCRIPT_REVIEW_REQUESTED`

**⚠️ 주의사항:**

- `SCRIPT_READY` 상태에서만 호출 가능
- 다른 상태에서 호출 시 400 에러

---

### 📌 Step 5.5: 1차 승인 (스크립트)

```bash
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/approve' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "videoId": "5e68abb2-98d3-4fcd-92aa-e398e7129946",
  "previousStatus": "SCRIPT_REVIEW_REQUESTED",
  "currentStatus": "SCRIPT_APPROVED",
  "updatedAt": "2025-12-18T10:05:00.000Z"
}
```

**✅ 확인사항:**

- `currentStatus`가 `SCRIPT_APPROVED`
- 이제 영상 생성이 가능해짐

---

### 📌 Step 6: 영상 자동생성 요청

```bash
curl -X POST 'http://localhost:9002/video/job' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "eduId": "'$EDUCATION_ID'",
    "scriptId": "'$SCRIPT_ID'",
    "videoId": "'$VIDEO_ID'"
  }'
```

**응답 예시 (MOCK):**

```json
{
  "jobId": "f8d7e6c5-b4a3-2190-8765-432109876543",
  "status": "COMPLETED"
}
```

**환경변수 설정:**

```bash
export JOB_ID="응답에서_받은_jobId"
```

**✅ 확인사항:**

- `SCRIPT_APPROVED` 상태에서만 호출 가능
- MOCK 처리로 즉시 `COMPLETED` 반환됨

**⚠️ 현재 상태 (MOCK):**

- AI 서버 미연동으로 즉시 완료 처리
- 실제 구현 시에는 `QUEUED` 상태로 시작하고 콜백으로 완료

---

### 📌 Step 7: 영상 상태 확인

```bash
curl -X GET 'http://localhost:9002/admin/videos/'$VIDEO_ID \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "id": "5e68abb2-98d3-4fcd-92aa-e398e7129946",
  "educationId": "...",
  "title": "2024년 직장 내 괴롭힘 예방 교육",
  "status": "READY",
  "fileUrl": "https://mock-cdn.example.com/videos/...",
  "duration": 600
}
```

**✅ 확인사항:**

- `status`가 `READY`
- `fileUrl`과 `duration`이 설정됨

---

### 📌 Step 8: 2차 검토 요청 (영상)

```bash
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/review-request' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "videoId": "5e68abb2-98d3-4fcd-92aa-e398e7129946",
  "previousStatus": "READY",
  "currentStatus": "FINAL_REVIEW_REQUESTED",
  "updatedAt": "2025-12-18T10:15:00.000Z"
}
```

**✅ 확인사항:**

- `previousStatus`가 `READY`
- `currentStatus`가 `FINAL_REVIEW_REQUESTED`

---

### 📌 Step 9: 2차 승인 (최종 게시)

```bash
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/approve' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "videoId": "5e68abb2-98d3-4fcd-92aa-e398e7129946",
  "previousStatus": "FINAL_REVIEW_REQUESTED",
  "currentStatus": "PUBLISHED",
  "updatedAt": "2025-12-18T10:20:00.000Z"
}
```

**✅ 확인사항:**

- `currentStatus`가 `PUBLISHED`
- 이제 유저에게 노출됨!

---

## 개발용 유틸리티 API

### 상태 강제 변경 (테스트용)

특정 상태로 강제 변경하여 테스트할 때 사용:

```bash
# SCRIPT_READY로 강제 변경
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/status?status=SCRIPT_READY' \
  -H 'Authorization: Bearer '$TOKEN

# SCRIPT_APPROVED로 강제 변경
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/status?status=SCRIPT_APPROVED' \
  -H 'Authorization: Bearer '$TOKEN

# READY로 강제 변경
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/status?status=READY' \
  -H 'Authorization: Bearer '$TOKEN
```

### 사용 가능한 상태

| 상태                      | 설명                       |
| ------------------------- | -------------------------- |
| `DRAFT`                   | 초기 생성                  |
| `SCRIPT_GENERATING`       | 스크립트 생성 중           |
| `SCRIPT_READY`            | 스크립트 생성 완료         |
| `SCRIPT_REVIEW_REQUESTED` | 1차 검토 요청 (스크립트)   |
| `SCRIPT_APPROVED`         | 1차 승인 (영상 생성 가능)  |
| `PROCESSING`              | 영상 생성 중               |
| `READY`                   | 영상 생성 완료             |
| `FINAL_REVIEW_REQUESTED`  | 2차 검토 요청 (영상)       |
| `PUBLISHED`               | 최종 승인/게시 (유저 노출) |

---

## 반려 테스트

### 1차 반려 (스크립트)

```bash
# 1. SCRIPT_REVIEW_REQUESTED 상태에서
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/reject' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{"reason": "스크립트 내용 수정 필요"}'
```

결과: `SCRIPT_REVIEW_REQUESTED` → `SCRIPT_READY`

### 2차 반려 (영상)

```bash
# 2. FINAL_REVIEW_REQUESTED 상태에서
curl -X PUT 'http://localhost:9002/admin/videos/'$VIDEO_ID'/reject' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{"reason": "영상 품질 개선 필요"}'
```

결과: `FINAL_REVIEW_REQUESTED` → `READY`

---

## 알려진 문제점 및 참고사항

### ⚠️ 1. FK 제약 조건 에러

**문제:**

```
education_script_source_doc_id_fkey 위반
```

**원인:**

- `materialId`는 `infra-service`의 `RagDocument.id`
- `education_script.source_doc_id`는 `education_source_doc` 테이블의 FK

**해결:**

- `EducationScript.sourceDocId`를 설정하지 않음 (이미 수정됨)
- `materialId`는 `EducationVideo`에 저장됨

---

### ⚠️ 2. MOCK 처리된 기능

현재 AI 서버 미연동으로 다음 기능은 MOCK 처리됨:

| 기능          | 현재 상태        | 설명                          |
| ------------- | ---------------- | ----------------------------- |
| 임베딩        | MOCK (즉시 완료) | `RagDocument.status` = QUEUED |
| 스크립트 생성 | 수동 콜백 필요   | `/script/complete` 직접 호출  |
| 영상 생성     | MOCK (즉시 완료) | `POST /video/job` 즉시 완료   |

---

### ⚠️ 3. 상태 전이 규칙

API는 현재 상태를 확인하여 자동 분기:

| API              | 현재 상태                 | 다음 상태                 |
| ---------------- | ------------------------- | ------------------------- |
| `review-request` | `SCRIPT_READY`            | `SCRIPT_REVIEW_REQUESTED` |
| `review-request` | `READY`                   | `FINAL_REVIEW_REQUESTED`  |
| `approve`        | `SCRIPT_REVIEW_REQUESTED` | `SCRIPT_APPROVED`         |
| `approve`        | `FINAL_REVIEW_REQUESTED`  | `PUBLISHED`               |
| `reject`         | `SCRIPT_REVIEW_REQUESTED` | `SCRIPT_READY`            |
| `reject`         | `FINAL_REVIEW_REQUESTED`  | `READY`                   |

---

### ⚠️ 4. 영상 생성 조건

`POST /video/job`은 **`SCRIPT_APPROVED` 상태에서만** 호출 가능.

다른 상태에서 호출 시:

```json
{
  "status": 400,
  "message": "영상 생성은 SCRIPT_APPROVED 상태에서만 가능합니다."
}
```

---

## 빠른 테스트 스크립트

전체 플로우를 한 번에 실행하는 스크립트:

```bash
#!/bin/bash

# 환경 변수 설정
TOKEN="YOUR_TOKEN_HERE"
EDUCATION_ID="YOUR_EDUCATION_ID"

# 1. 영상 생성
VIDEO_RESPONSE=$(curl -s -X POST 'http://localhost:9002/admin/videos' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"educationId": "'$EDUCATION_ID'", "title": "테스트 교육 영상"}')
VIDEO_ID=$(echo $VIDEO_RESPONSE | jq -r '.videoId')
echo "VIDEO_ID: $VIDEO_ID"

# 2. 상태를 SCRIPT_READY로 변경 (스크립트 생성 스킵)
curl -s -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/status?status=SCRIPT_READY" \
  -H "Authorization: Bearer $TOKEN"

# 3. 1차 검토 요청
curl -s -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/review-request" \
  -H "Authorization: Bearer $TOKEN"

# 4. 1차 승인
curl -s -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/approve" \
  -H "Authorization: Bearer $TOKEN"

# 5. 상태를 READY로 변경 (영상 생성 스킵)
curl -s -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/status?status=READY" \
  -H "Authorization: Bearer $TOKEN"

# 6. 2차 검토 요청
curl -s -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/review-request" \
  -H "Authorization: Bearer $TOKEN"

# 7. 2차 승인 (게시)
curl -s -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/approve" \
  -H "Authorization: Bearer $TOKEN"

# 8. 최종 상태 확인
curl -s -X GET "http://localhost:9002/admin/videos/$VIDEO_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## 체크리스트

- [ ] JWT 토큰 발급 완료
- [ ] educationId 확인 완료
- [ ] Step 1: 영상 컨텐츠 생성 (DRAFT)
- [ ] Step 4.5: 스크립트 콜백 (또는 상태 강제 변경)
- [ ] Step 5: 1차 검토 요청
- [ ] Step 5.5: 1차 승인
- [ ] Step 6: 영상 생성 (또는 상태 강제 변경)
- [ ] Step 8: 2차 검토 요청
- [ ] Step 9: 2차 승인 (PUBLISHED)
- [ ] 최종 상태 확인: `status: PUBLISHED`
