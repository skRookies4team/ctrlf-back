# FAQ 서비스 테스트 시나리오

## 테스트 환경

```
chat-service: http://localhost:9005
ai-server: http://localhost:8000
ragflow: http://localhost:8080
```

## 사전 준비

### 1. JWT 토큰 발급

Keycloak에서 토큰을 발급받아 환경변수로 설정합니다.

```bash
# 토큰 발급 (예시)
export TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6..."
```

### 2. operatorId 확인

JWT 토큰에서 `sub` 클레임을 확인하거나, 기존 FAQ 목록에서 확인합니다.

```bash
export OPERATOR_ID="076d9ad4-a3b8-4853-95fe-7c427c8bc529"
```

### 3. AI 서버 및 RAGFlow 서버 실행 확인

```bash
# AI 서버 상태 확인
curl -X GET 'http://localhost:8000/health'

# RAGFlow 서버 상태 확인
curl -X GET 'http://localhost:8080/health'
```

---

## 전체 테스트 시나리오

### 📌 Step 1: FAQ 수동 생성

```bash
curl -X POST 'http://localhost:9005/chat/faq' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "question": "비밀번호를 잊어버렸어요",
    "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다.",
    "domain": "SECURITY",
    "priority": 1
  }'
```

**응답 예시:**

```json
"239d0429-b517-4897-beb0-bd1f699999da"
```

**환경변수 설정:**

```bash
export FAQ_ID="응답에서_받은_FAQ_ID"
```

**✅ 확인사항:**

- FAQ ID가 UUID 형식인지 확인
- FAQ가 즉시 활성화되었는지 확인 (`isActive: true`)

**⚠️ 주의사항:**

- `domain`은 SECURITY, POLICY, EDUCATION 등 유효한 값이어야 함
- `priority`는 1~5 사이의 값이어야 함

---

### 📌 Step 2: FAQ 수정

```bash
curl -X PATCH 'http://localhost:9005/chat/faq/'$FAQ_ID \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "question": "비밀번호를 잊어버렸어요 (수정)",
    "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다. (수정)",
    "priority": 2
  }'
```

**응답:** `200 OK` (No Content)

**✅ 확인사항:**

- FAQ가 수정되었는지 확인
- 선택적 필드만 수정되는지 확인

---

### 📌 Step 3: FAQ 목록 조회 (관리자)

```bash
curl -X GET 'http://localhost:9005/chat/faq' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "question": "비밀번호를 잊어버렸어요 (수정)",
    "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다. (수정)",
    "domain": "SECURITY",
    "isActive": true,
    "priority": 2,
    "createdAt": "2025-12-24T02:00:00Z",
    "updatedAt": "2025-12-24T02:01:00Z"
  }
]
```

**✅ 확인사항:**

- 모든 FAQ가 반환되는지 확인 (활성/비활성 포함)
- 수정된 내용이 반영되었는지 확인

---

### 📌 Step 4: FAQ 홈 조회 (사용자)

```bash
curl -X GET 'http://localhost:9005/faq/home' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "domain": "SECURITY",
    "question": "비밀번호를 잊어버렸어요 (수정)",
    "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다. (수정)",
    "publishedAt": "2025-12-24T02:00:00Z"
  }
]
```

**✅ 확인사항:**

- 도메인별로 1개씩만 반환되는지 확인
- `isActive: true`인 FAQ만 노출되는지 확인

---

### 📌 Step 5: 도메인별 FAQ 조회

```bash
curl -X GET 'http://localhost:9005/faq?domain=SECURITY' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "domain": "SECURITY",
    "question": "비밀번호를 잊어버렸어요 (수정)",
    "answer": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다. (수정)",
    "publishedAt": "2025-12-24T02:00:00Z"
  }
]
```

**✅ 확인사항:**

- 특정 도메인의 FAQ만 반환되는지 확인
- `priority` 순으로 정렬되는지 확인

---

### 📌 Step 6: FAQ 후보 생성

```bash
curl -X POST 'http://localhost:9005/admin/faq/candidates' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "question": "FAQ 후보 질문",
    "domain": "SECURITY"
  }'
```

**응답 예시:**

```json
"24454c70-0079-4c8b-9860-73a636a979b6"
```

**환경변수 설정:**

```bash
export CANDIDATE_ID="응답에서_받은_CANDIDATE_ID"
```

**✅ 확인사항:**

- 후보 ID가 UUID 형식인지 확인
- 후보 상태가 `NEW`인지 확인

---

### 📌 Step 7: FAQ 후보 목록 조회

```bash
curl -X GET 'http://localhost:9005/admin/faq/candidates' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
[
  {
    "id": "24454c70-0079-4c8b-9860-73a636a979b6",
    "canonicalQuestion": "FAQ 후보 질문",
    "domain": "SECURITY",
    "questionCount7d": 0,
    "questionCount30d": 0,
    "avgIntentConfidence": null,
    "piiDetected": false,
    "scoreCandidate": null,
    "status": "NEW",
    "lastAskedAt": null,
    "createdAt": "2025-12-24T02:00:00Z"
  }
]
```

**✅ 확인사항:**

- 생성한 후보가 목록에 포함되어 있는지 확인
- 필터링 옵션 (`domain`, `status`)이 작동하는지 확인

---

### 📌 Step 8: FAQ 초안 자동 생성 (AI + RAGFlow)

```bash
curl -X POST 'http://localhost:9005/admin/faq/candidates/'$CANDIDATE_ID'/generate' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "draftId": "350d0429-b517-4897-beb0-bd1f699999db"
}
```

**환경변수 설정:**

```bash
export DRAFT_ID="응답에서_받은_draftId"
```

**✅ 확인사항:**

- Draft ID가 UUID 형식인지 확인
- AI 서버가 정상적으로 호출되었는지 확인
- RAGFlow 검색이 정상적으로 수행되었는지 확인

**⚠️ 주의사항:**

- AI 서버가 실행 중이어야 함
- RAGFlow 서버가 실행 중이어야 함
- PII가 감지되었거나 신뢰도가 0.7 미만이면 실패

**에러 케이스:**

- `NO_DOCS_FOUND`: RAGFlow 검색 결과 없음
- `500 Internal Server Error`: AI 서버 또는 RAGFlow 연결 실패

---

### 📌 Step 9: FAQ Draft 목록 조회

```bash
curl -X GET 'http://localhost:9005/admin/faq/drafts' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
[
  {
    "id": "350d0429-b517-4897-beb0-bd1f699999db",
    "domain": "SECURITY",
    "question": "FAQ 후보 질문",
    "summary": "FAQ 초안 요약",
    "status": "DRAFT",
    "createdAt": "2025-12-24T02:00:00"
  }
]
```

**✅ 확인사항:**

- 생성한 Draft가 목록에 포함되어 있는지 확인
- 필터링 옵션 (`domain`, `status`)이 작동하는지 확인

---

### 📌 Step 10: FAQ Draft 승인

**⚠️ 주의**: Query Parameter의 한글은 URL 인코딩이 필요합니다.

```bash
# URL 인코딩 예시 (PowerShell)
$question = [System.Web.HttpUtility]::UrlEncode("승인할 질문")
$answer = [System.Web.HttpUtility]::UrlEncode("승인할 답변")

curl -X POST "http://localhost:9005/admin/faq/drafts/$DRAFT_ID/approve?reviewerId=$OPERATOR_ID&question=$question&answer=$answer" \
  -H 'Authorization: Bearer '$TOKEN
```

**응답:** `200 OK` (No Content)

**✅ 확인사항:**

- 새로운 FAQ가 생성되었는지 확인
- Draft 상태가 `PUBLISHED`로 변경되었는지 확인
- 생성된 FAQ가 `isActive: true`인지 확인

---

### 📌 Step 11: FAQ Draft 반려

**⚠️ 주의**: Query Parameter의 한글은 URL 인코딩이 필요합니다.

```bash
# URL 인코딩 예시 (PowerShell)
$reason = [System.Web.HttpUtility]::UrlEncode("반려 사유")

curl -X POST "http://localhost:9005/admin/faq/drafts/$DRAFT_ID/reject?reviewerId=$OPERATOR_ID&reason=$reason" \
  -H 'Authorization: Bearer '$TOKEN
```

**응답:** `200 OK` (No Content)

**✅ 확인사항:**

- Draft 상태가 `REJECTED`로 변경되었는지 확인
- FAQ가 생성되지 않았는지 확인

---

### 📌 Step 12: UI 카테고리 생성

**⚠️ 주의**: `slug`는 고유해야 합니다. 중복 시 오류가 발생합니다.

```bash
# 고유 slug 생성 (타임스탬프 기반)
TIMESTAMP=$(date +%Y%m%d%H%M%S)
UNIQUE_SLUG="test-category-$TIMESTAMP"

curl -X POST "http://localhost:9005/admin/faq/ui-categories?operatorId=$OPERATOR_ID" \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "slug": "'$UNIQUE_SLUG'",
    "displayName": "테스트 UI 카테고리",
    "sortOrder": 1
  }'
```

**응답 예시:**

```json
"239d0429-b517-4897-beb0-bd1f699999da"
```

**환경변수 설정:**

```bash
export CATEGORY_ID="응답에서_받은_CATEGORY_ID"
```

**✅ 확인사항:**

- 카테고리 ID가 UUID 형식인지 확인
- `slug`가 고유한지 확인

**에러 케이스:**

- `400 Bad Request`: "이미 존재하는 slug 입니다."

---

### 📌 Step 13: UI 카테고리 목록 조회

```bash
curl -X GET 'http://localhost:9005/admin/faq/ui-categories' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "slug": "test-category-20251224120000",
    "displayName": "테스트 UI 카테고리",
    "sortOrder": 1,
    "isActive": true,
    "createdAt": "2025-12-24T02:00:00Z",
    "updatedAt": "2025-12-24T02:00:00Z"
  }
]
```

**✅ 확인사항:**

- 생성한 카테고리가 목록에 포함되어 있는지 확인

---

### 📌 Step 14: UI 카테고리 수정

```bash
curl -X PATCH "http://localhost:9005/admin/faq/ui-categories/$CATEGORY_ID?operatorId=$OPERATOR_ID" \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "displayName": "테스트 UI 카테고리 (수정)",
    "sortOrder": 2
  }'
```

**응답:** `200 OK` (No Content)

**✅ 확인사항:**

- 카테고리가 수정되었는지 확인

---

### 📌 Step 15: FAQ 대시보드 조회

```bash
# 전체 대시보드
curl -X GET 'http://localhost:9005/faq/dashboard/home' \
  -H 'Authorization: Bearer '$TOKEN

# 도메인별 대시보드
curl -X GET 'http://localhost:9005/faq/dashboard/SECURITY' \
  -H 'Authorization: Bearer '$TOKEN
```

**✅ 확인사항:**

- 통계 정보가 정상적으로 반환되는지 확인

---

## 에러 케이스 테스트

### 1. 존재하지 않는 FAQ 조회

```bash
curl -X GET 'http://localhost:9005/chat/faq/00000000-0000-0000-0000-000000000000' \
  -H 'Authorization: Bearer '$TOKEN
```

**예상 응답:** `404 Not Found`

---

### 2. 잘못된 도메인으로 FAQ 생성

```bash
curl -X POST 'http://localhost:9005/chat/faq' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "question": "테스트",
    "answer": "테스트 답변",
    "domain": "INVALID_DOMAIN",
    "priority": 1
  }'
```

**예상 응답:** `400 Bad Request`

---

### 3. 중복된 slug로 UI 카테고리 생성

```bash
curl -X POST "http://localhost:9005/admin/faq/ui-categories?operatorId=$OPERATOR_ID" \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "slug": "duplicate-slug",
    "displayName": "중복 테스트",
    "sortOrder": 1
  }'

# 동일한 slug로 다시 생성
curl -X POST "http://localhost:9005/admin/faq/ui-categories?operatorId=$OPERATOR_ID" \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "slug": "duplicate-slug",
    "displayName": "중복 테스트 2",
    "sortOrder": 1
  }'
```

**예상 응답:** `400 Bad Request`: "이미 존재하는 slug 입니다."

---

### 4. PII 감지된 후보로 Draft 생성

```bash
# PII가 감지된 후보 ID 사용
curl -X POST 'http://localhost:9005/admin/faq/candidates/PII_CANDIDATE_ID/generate' \
  -H 'Authorization: Bearer '$TOKEN
```

**예상 응답:** `400 Bad Request`: "PII가 감지된 FAQ 후보는 Draft를 생성할 수 없습니다."

---

### 5. 이미 승인된 Draft 재승인

```bash
curl -X POST "http://localhost:9005/admin/faq/drafts/$DRAFT_ID/approve?reviewerId=$OPERATOR_ID&question=$question&answer=$answer" \
  -H 'Authorization: Bearer '$TOKEN
```

**예상 응답:** `400 Bad Request`: "이미 승인된 FAQ 초안입니다."

---

---

## 개발용 유틸리티 API

### FAQ 상태 확인

특정 FAQ의 상세 정보를 확인할 때 사용:

```bash
curl -X GET 'http://localhost:9005/chat/faq' \
  -H 'Authorization: Bearer '$TOKEN | jq '.[] | select(.id == "'$FAQ_ID'")'
```

### Draft 상태 확인

특정 Draft의 상세 정보를 확인할 때 사용:

```bash
curl -X GET 'http://localhost:9005/admin/faq/drafts' \
  -H 'Authorization: Bearer '$TOKEN | jq '.[] | select(.id == "'$DRAFT_ID'")'
```

---

## 알려진 문제점 및 참고사항

### ⚠️ 1. RAGFlow 검색 결과 없음

**문제:**

FAQ 초안 생성 시 `NO_DOCS_FOUND` 오류 발생

**원인:**

- AI 서버가 RAGFlow를 호출하여 검색 수행
- Mock RAGFlow 서버의 `retrieval_results.json`에 해당 질문과 유사한 데이터가 없음

**해결:**

- Mock 서버의 `retrieval_results.json`에 테스트 데이터 추가
- 더 일반적인 질문으로 테스트
- RAGFlow 서버의 데이터 매칭 로직 개선

---

### ⚠️ 2. UI 카테고리 Slug 중복

**문제:**

동일한 slug로 카테고리 생성 시 오류 발생

**원인:**

- `slug` 필드에 고유 제약 조건이 있음

**해결:**

- 고유 slug 생성 (타임스탬프 기반)
- `test-category-{timestamp}` 형식 사용

---

### ⚠️ 3. URL 인코딩 필요

**영향받는 API:**

- `POST /admin/faq/drafts/{draftId}/approve` (한글 파라미터)
- `POST /admin/faq/drafts/{draftId}/reject` (한글 파라미터)
- `POST /admin/faq/ui-categories/{categoryId}/deactivate` (한글 파라미터)

**해결:**

Query Parameter의 한글을 URL 인코딩

**예시 (PowerShell)**:

```powershell
$question = [System.Web.HttpUtility]::UrlEncode("승인할 질문")
$answer = [System.Web.HttpUtility]::UrlEncode("승인할 답변")
```

---

### ⚠️ 4. Draft 생성 조건

`POST /admin/faq/candidates/{candidateId}/generate`는 다음 조건을 만족해야 함:

- `piiDetected: false` (PII가 감지되지 않아야 함)
- `avgIntentConfidence >= 0.7` (의도 신뢰도 0.7 이상)

조건을 만족하지 않으면:

```json
{
  "error": "BAD_REQUEST",
  "message": "PII가 감지된 FAQ 후보는 Draft를 생성할 수 없습니다."
}
```

또는

```json
{
  "error": "BAD_REQUEST",
  "message": "의도 신뢰도가 부족합니다. (현재: 0.65, 최소 요구: 0.7)"
}
```

---

## 빠른 테스트 스크립트

전체 플로우를 한 번에 실행하는 스크립트:

```bash
#!/bin/bash

# 환경 변수 설정
TOKEN="YOUR_TOKEN_HERE"
OPERATOR_ID="YOUR_OPERATOR_ID"

# 1. FAQ 생성
FAQ_RESPONSE=$(curl -s -X POST 'http://localhost:9005/chat/faq' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "question": "테스트 FAQ 질문",
    "answer": "테스트 FAQ 답변",
    "domain": "SECURITY",
    "priority": 1
  }')
FAQ_ID=$(echo $FAQ_RESPONSE | tr -d '"')
echo "FAQ_ID: $FAQ_ID"

# 2. FAQ 조회
curl -s -X GET "http://localhost:9005/chat/faq" \
  -H "Authorization: Bearer $TOKEN" | jq

# 3. FAQ 후보 생성
CANDIDATE_RESPONSE=$(curl -s -X POST 'http://localhost:9005/admin/faq/candidates' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "question": "테스트 후보 질문",
    "domain": "SECURITY"
  }')
CANDIDATE_ID=$(echo $CANDIDATE_RESPONSE | tr -d '"')
echo "CANDIDATE_ID: $CANDIDATE_ID"

# 4. UI 카테고리 생성 (고유 slug)
TIMESTAMP=$(date +%Y%m%d%H%M%S)
UNIQUE_SLUG="test-category-$TIMESTAMP"

CATEGORY_RESPONSE=$(curl -s -X POST "http://localhost:9005/admin/faq/ui-categories?operatorId=$OPERATOR_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "slug": "'$UNIQUE_SLUG'",
    "displayName": "테스트 UI 카테고리",
    "sortOrder": 1
  }')
CATEGORY_ID=$(echo $CATEGORY_RESPONSE | tr -d '"')
echo "CATEGORY_ID: $CATEGORY_ID"

echo "테스트 완료!"
```

---

## 체크리스트

- [ ] JWT 토큰 발급 완료
- [ ] operatorId 확인 완료
- [ ] AI 서버 및 RAGFlow 서버 실행 확인
- [ ] Step 1: FAQ 수동 생성
- [ ] Step 2: FAQ 수정
- [ ] Step 3: FAQ 목록 조회 (관리자)
- [ ] Step 4: FAQ 홈 조회 (사용자)
- [ ] Step 5: 도메인별 FAQ 조회
- [ ] Step 6: FAQ 후보 생성
- [ ] Step 7: FAQ 후보 목록 조회
- [ ] Step 8: FAQ 초안 자동 생성 (AI + RAGFlow)
- [ ] Step 9: FAQ Draft 목록 조회
- [ ] Step 10: FAQ Draft 승인
- [ ] Step 11: FAQ Draft 반려
- [ ] Step 12: UI 카테고리 생성
- [ ] Step 13: UI 카테고리 목록 조회
- [ ] Step 14: UI 카테고리 수정
- [ ] Step 15: FAQ 대시보드 조회
- [ ] 에러 케이스 테스트

---

**문서 버전**: 2025-12-24  
**작성자**: AI Assistant

