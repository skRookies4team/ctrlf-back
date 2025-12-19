# 교육 영상 API 테스트 결과 리포트

**테스트 일시**: 2025-12-18 22:25 KST  
**테스트 환경**: Local (education-service: 9002, infra-service: 9003, Keycloak: 8090)  
**테스트 계정**: creator1 (VIDEO_CREATOR 권한)

---

## 1. 테스트 환경 설정

### 1.1 토큰 발급

```bash
curl -s -X POST 'http://localhost:8090/realms/ctrlf/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=infra-admin' \
  -d 'client_secret=changeme' \
  -d 'username=creator1' \
  -d 'password=44444' | jq -r '.access_token'
```

### 1.2 사용 가능한 테스트 계정

| 계정      | 비밀번호 | 역할              | 용도               |
| --------- | -------- | ----------------- | ------------------ |
| user1     | 11111    | EMPLOYEE          | 일반 직원 (유저용) |
| admin1    | 22222    | SYSTEM_ADMIN      | 시스템 관리자      |
| reviewer1 | 33333    | CONTENTS_REVIEWER | 콘텐츠 검토자      |
| creator1  | 44444    | VIDEO_CREATOR     | 교육 영상 제작자   |

---

## 2. 테스트 1: 정상 플로우 (승인)

### 2.1 테스트 대상 영상

- **videoId**: `0525a670-f36c-4382-a17d-eb2feb1c7ef2`
- **educationId**: `a2f53c87-e157-4ce4-b287-e6f000d96161`
- **title**: 2024년 직장 내 괴롭힘 예방 교육 테스트

### 2.2 테스트 과정

#### Step 1: 영상 컨텐츠 생성 (DRAFT)

```bash
curl -X POST 'http://localhost:9002/admin/videos' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "educationId": "a2f53c87-e157-4ce4-b287-e6f000d96161",
    "title": "2024년 직장 내 괴롭힘 예방 교육 테스트",
    "departmentScope": null
  }'
```

**응답**:

```json
{
  "videoId": "0525a670-f36c-4382-a17d-eb2feb1c7ef2",
  "status": "DRAFT"
}
```

---

#### Step 4.5 (MOCK): 스크립트 생성 완료 시뮬레이션

```bash
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/status?status=SCRIPT_READY" \
  -H "Authorization: Bearer $TOKEN"
```

**응답**:

```json
{
  "videoId": "0525a670-f36c-4382-a17d-eb2feb1c7ef2",
  "previousStatus": "DRAFT",
  "currentStatus": "SCRIPT_READY",
  "updatedAt": "2025-12-18T13:25:12.140505Z"
}
```

---

#### Step 5: 1차 검토 요청 (스크립트)

```bash
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/review-request" \
  -H "Authorization: Bearer $TOKEN"
```

**응답**:

```json
{
  "videoId": "0525a670-f36c-4382-a17d-eb2feb1c7ef2",
  "previousStatus": "SCRIPT_READY",
  "currentStatus": "SCRIPT_REVIEW_REQUESTED",
  "updatedAt": "2025-12-18T13:25:45.033582Z"
}
```

---

#### Step 5.5: 1차 승인 (스크립트)

```bash
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/approve" \
  -H "Authorization: Bearer $TOKEN"
```

**응답**:

```json
{
  "videoId": "0525a670-f36c-4382-a17d-eb2feb1c7ef2",
  "previousStatus": "SCRIPT_REVIEW_REQUESTED",
  "currentStatus": "SCRIPT_APPROVED",
  "updatedAt": "2025-12-18T13:25:45.057528Z"
}
```

---

#### Step 6 (MOCK): 영상 생성 완료 시뮬레이션

```bash
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/status?status=READY" \
  -H "Authorization: Bearer $TOKEN"
```

**응답**:

```json
{
  "videoId": "0525a670-f36c-4382-a17d-eb2feb1c7ef2",
  "previousStatus": "SCRIPT_APPROVED",
  "currentStatus": "READY",
  "updatedAt": "2025-12-18T13:25:45.077804Z"
}
```

---

#### Step 8: 2차 검토 요청 (영상)

```bash
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/review-request" \
  -H "Authorization: Bearer $TOKEN"
```

**응답**:

```json
{
  "videoId": "0525a670-f36c-4382-a17d-eb2feb1c7ef2",
  "previousStatus": "READY",
  "currentStatus": "FINAL_REVIEW_REQUESTED",
  "updatedAt": "2025-12-18T13:25:45.092805Z"
}
```

---

#### Step 9: 2차 승인 (최종 게시)

```bash
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/approve" \
  -H "Authorization: Bearer $TOKEN"
```

**응답**:

```json
{
  "videoId": "0525a670-f36c-4382-a17d-eb2feb1c7ef2",
  "previousStatus": "FINAL_REVIEW_REQUESTED",
  "currentStatus": "PUBLISHED",
  "updatedAt": "2025-12-18T13:25:45.107753Z"
}
```

---

#### 최종 상태 확인

```bash
curl -X GET "http://localhost:9002/admin/videos/$VIDEO_ID" \
  -H "Authorization: Bearer $TOKEN"
```

**응답**:

```json
{
  "id": "0525a670-f36c-4382-a17d-eb2feb1c7ef2",
  "educationId": "a2f53c87-e157-4ce4-b287-e6f000d96161",
  "title": "2024년 직장 내 괴롭힘 예방 교육 테스트",
  "generationJobId": null,
  "fileUrl": null,
  "version": 1,
  "duration": null,
  "status": "PUBLISHED",
  "targetDeptCode": null,
  "departmentScope": null,
  "orderIndex": 0,
  "createdAt": "2025-12-18T12:49:11.677199Z"
}
```

### 2.3 상태 전이 요약

```
DRAFT → SCRIPT_READY → SCRIPT_REVIEW_REQUESTED → SCRIPT_APPROVED → READY → FINAL_REVIEW_REQUESTED → PUBLISHED
```

| 단계 | 이전 상태               | 현재 상태               | 결과 |
| ---- | ----------------------- | ----------------------- | ---- |
| 1    | (생성)                  | DRAFT                   | ✅   |
| 4.5  | DRAFT                   | SCRIPT_READY            | ✅   |
| 5    | SCRIPT_READY            | SCRIPT_REVIEW_REQUESTED | ✅   |
| 5.5  | SCRIPT_REVIEW_REQUESTED | SCRIPT_APPROVED         | ✅   |
| 6    | SCRIPT_APPROVED         | READY                   | ✅   |
| 8    | READY                   | FINAL_REVIEW_REQUESTED  | ✅   |
| 9    | FINAL_REVIEW_REQUESTED  | **PUBLISHED**           | ✅   |

---

## 3. 테스트 2: 반려 플로우

### 3.1 테스트 대상 영상

- **videoId**: `1f3603bf-66b4-4db1-96df-397e79167193`
- **title**: 반려 테스트 영상

### 3.2 1차 반려 (스크립트)

```bash
# 상태를 SCRIPT_REVIEW_REQUESTED로 변경
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/status?status=SCRIPT_REVIEW_REQUESTED" \
  -H "Authorization: Bearer $TOKEN"

# 반려
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/reject" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "스크립트 내용 수정 필요"}'
```

**응답**:

```json
{
  "videoId": "1f3603bf-66b4-4db1-96df-397e79167193",
  "previousStatus": "SCRIPT_REVIEW_REQUESTED",
  "currentStatus": "SCRIPT_READY",
  "updatedAt": "2025-12-18T13:26:11.344710Z"
}
```

**결과**: `SCRIPT_REVIEW_REQUESTED` → `SCRIPT_READY` ✅

### 3.3 2차 반려 (영상)

```bash
# 상태를 FINAL_REVIEW_REQUESTED로 변경
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/status?status=FINAL_REVIEW_REQUESTED" \
  -H "Authorization: Bearer $TOKEN"

# 반려
curl -X PUT "http://localhost:9002/admin/videos/$VIDEO_ID/reject" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "영상 품질 개선 필요"}'
```

**응답**:

```json
{
  "videoId": "1f3603bf-66b4-4db1-96df-397e79167193",
  "previousStatus": "FINAL_REVIEW_REQUESTED",
  "currentStatus": "READY",
  "updatedAt": "2025-12-18T13:26:11.376210Z"
}
```

**결과**: `FINAL_REVIEW_REQUESTED` → `READY` ✅

---

## 4. 테스트 결과 요약

### 4.1 API 테스트 결과

| API                                     | 기능                     | 결과 |
| --------------------------------------- | ------------------------ | ---- |
| `POST /admin/videos`                    | 영상 컨텐츠 생성 (DRAFT) | ✅   |
| `PUT /admin/videos/{id}/status`         | 상태 강제 변경 (개발용)  | ✅   |
| `PUT /admin/videos/{id}/review-request` | 검토 요청 (1차/2차 자동) | ✅   |
| `PUT /admin/videos/{id}/approve`        | 승인 (1차/2차 자동)      | ✅   |
| `PUT /admin/videos/{id}/reject`         | 반려 (1차/2차 자동)      | ✅   |
| `GET /admin/videos/{id}`                | 영상 상세 조회           | ✅   |

### 4.2 상태 전이 로직 검증

| 현재 상태               | review-request 후       | approve 후      | reject 후    |
| ----------------------- | ----------------------- | --------------- | ------------ |
| SCRIPT_READY            | SCRIPT_REVIEW_REQUESTED | -               | -            |
| SCRIPT_REVIEW_REQUESTED | -                       | SCRIPT_APPROVED | SCRIPT_READY |
| READY                   | FINAL_REVIEW_REQUESTED  | -               | -            |
| FINAL_REVIEW_REQUESTED  | -                       | **PUBLISHED**   | READY        |

---

## 5. DB 확인 방법

### 5.1 PostgreSQL 접속

```bash
# Docker 컨테이너로 접속하는 경우
docker exec -it postgres psql -U postgres -d education_db

# 또는 psql 직접 접속
psql -h localhost -p 5432 -U postgres -d education_db
```

### 5.2 테스트 영상 조회

```sql
-- 생성된 영상 목록 조회
SELECT id, title, status, created_at
FROM education_video
ORDER BY created_at DESC
LIMIT 10;

-- 특정 영상 상세 조회
SELECT * FROM education_video
WHERE id = '0525a670-f36c-4382-a17d-eb2feb1c7ef2';
```

**예상 결과**:

```
                  id                  |                    title                     |   status   |          created_at
--------------------------------------+----------------------------------------------+------------+------------------------------
 0525a670-f36c-4382-a17d-eb2feb1c7ef2 | 2024년 직장 내 괴롭힘 예방 교육 테스트         | PUBLISHED  | 2025-12-18 12:49:11.677199
 1f3603bf-66b4-4db1-96df-397e79167193 | 반려 테스트 영상                              | READY      | 2025-12-18 13:26:11.xxx
```

### 5.3 교육별 영상 수 확인

```sql
SELECT
  e.id as education_id,
  e.category,
  COUNT(v.id) as video_count,
  COUNT(CASE WHEN v.status = 'PUBLISHED' THEN 1 END) as published_count
FROM education e
LEFT JOIN education_video v ON e.id = v.education_id
GROUP BY e.id, e.category;
```

### 5.4 상태별 영상 수 확인

```sql
SELECT status, COUNT(*) as count
FROM education_video
GROUP BY status
ORDER BY count DESC;
```

### 5.5 스크립트 연결 확인

```sql
-- 영상-스크립트 연결 확인
SELECT
  v.id as video_id,
  v.title,
  v.script_id,
  s.version as script_version,
  s.raw_payload IS NOT NULL as has_script
FROM education_video v
LEFT JOIN education_script s ON v.script_id = s.id
WHERE v.id = '0525a670-f36c-4382-a17d-eb2feb1c7ef2';
```

### 5.6 전체 데이터 정리 (테스트 후 정리용)

```sql
-- ⚠️ 주의: 테스트 데이터 삭제
DELETE FROM education_video
WHERE title LIKE '%테스트%';

-- 또는 특정 ID 삭제
DELETE FROM education_video
WHERE id IN (
  '0525a670-f36c-4382-a17d-eb2feb1c7ef2',
  '1f3603bf-66b4-4db1-96df-397e79167193'
);
```

---

## 6. 발견된 이슈 및 참고사항

### 6.1 정상 작동 확인

- ✅ 영상 생성 API (DRAFT 상태)
- ✅ 상태 강제 변경 API (개발 테스트용)
- ✅ 1차/2차 검토 요청 자동 분기
- ✅ 1차/2차 승인 자동 분기
- ✅ 1차/2차 반려 자동 분기
- ✅ 영상 상세 조회 API

### 6.2 MOCK 처리 항목

현재 AI 서버 미연동으로 다음 기능은 MOCK 처리됨:

| 기능          | 현재 상태      | 우회 방법                              |
| ------------- | -------------- | -------------------------------------- |
| 스크립트 생성 | 수동 콜백 필요 | 상태 강제 변경 (`status=SCRIPT_READY`) |
| 영상 생성     | 수동 콜백 필요 | 상태 강제 변경 (`status=READY`)        |

### 6.3 NULL 필드 (정상)

최종 상태에서 다음 필드는 null:

- `fileUrl`: AI 영상 생성 콜백에서 설정됨
- `duration`: AI 영상 생성 콜백에서 설정됨
- `scriptId`: 스크립트 생성 콜백에서 연결됨
- `materialId`: 스크립트 생성 요청 시 연결됨
- `generationJobId`: 영상 생성 요청 시 생성됨

---

## 7. 다음 테스트 항목 (선택)

- [ ] 실제 스크립트 콜백 테스트 (`POST /script/complete`)
- [ ] 실제 영상 생성 콜백 테스트 (`POST /video/job/{jobId}/complete`)
- [ ] infra-service 연동 테스트 (S3 업로드, RAG 문서 등록)
- [ ] 유저 권한별 접근 테스트 (EMPLOYEE가 PUBLISHED 영상만 조회 가능한지)
