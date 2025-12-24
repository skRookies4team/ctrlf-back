# 채팅 서비스 테스트 시나리오

## 테스트 환경

```
chat-service: http://localhost:9001
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

### 2. userUuid 확인

JWT 토큰에서 `sub` 클레임을 확인하거나, 기존 세션 목록에서 userUuid를 확인합니다.

```bash
curl -X GET 'http://localhost:9001/api/chat/sessions' \
  -H 'Authorization: Bearer '$TOKEN
```

응답에서 `userUuid`를 확인하여 환경변수로 설정:

```bash
export USER_UUID="실제_사용자_UUID"
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

### 📌 Step 1: 채팅 세션 생성

```bash
curl -X POST 'http://localhost:9001/api/chat/sessions' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "userUuid": "'$USER_UUID'",
    "title": "보안 관련 질문",
    "domain": "SECURITY"
  }'
```

**응답 예시:**

```json
{
  "id": "239d0429-b517-4897-beb0-bd1f699999da",
  "title": "보안 관련 질문",
  "domain": "SECURITY",
  "userUuid": "076d9ad4-a3b8-4853-95fe-7c427c8bc529",
  "createdAt": "2025-12-24T02:00:00Z",
  "updatedAt": "2025-12-24T02:00:00Z"
}
```

**환경변수 설정:**

```bash
export SESSION_ID="응답에서_받은_id"
```

**✅ 확인사항:**

- `id`가 UUID 형식인지 확인
- `domain`이 올바르게 설정되었는지 확인
- `createdAt`과 `updatedAt`이 설정되었는지 확인

**⚠️ 주의사항:**

- `userUuid`가 JWT 토큰의 `sub`와 일치해야 함
- `domain`은 SECURITY, POLICY, EDUCATION 등 유효한 값이어야 함

---

### 📌 Step 2: 세션 단건 조회

```bash
curl -X GET 'http://localhost:9001/api/chat/sessions/'$SESSION_ID \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "id": "239d0429-b517-4897-beb0-bd1f699999da",
  "title": "보안 관련 질문",
  "domain": "SECURITY",
  "userUuid": "076d9ad4-a3b8-4853-95fe-7c427c8bc529",
  "createdAt": "2025-12-24T02:00:00Z",
  "updatedAt": "2025-12-24T02:00:00Z"
}
```

**✅ 확인사항:**

- 응답이 Step 1과 동일한지 확인
- 세션이 정상적으로 조회되는지 확인

---

### 📌 Step 3: 세션 목록 조회

```bash
curl -X GET 'http://localhost:9001/api/chat/sessions' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
[
  {
    "id": "239d0429-b517-4897-beb0-bd1f699999da",
    "title": "보안 관련 질문",
    "domain": "SECURITY",
    "userUuid": "076d9ad4-a3b8-4853-95fe-7c427c8bc529",
    "createdAt": "2025-12-24T02:00:00Z",
    "updatedAt": "2025-12-24T02:00:00Z"
  }
]
```

**✅ 확인사항:**

- 생성한 세션이 목록에 포함되어 있는지 확인
- 배열 형태로 반환되는지 확인

---

### 📌 Step 4: 세션 수정

```bash
curl -X PUT 'http://localhost:9001/api/chat/sessions/'$SESSION_ID \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "수정된 세션 제목"
  }'
```

**응답 예시:**

```json
{
  "id": "239d0429-b517-4897-beb0-bd1f699999da",
  "title": "수정된 세션 제목",
  "domain": "SECURITY",
  "userUuid": "076d9ad4-a3b8-4853-95fe-7c427c8bc529",
  "createdAt": "2025-12-24T02:00:00Z",
  "updatedAt": "2025-12-24T02:01:00Z"
}
```

**✅ 확인사항:**

- `title`이 수정되었는지 확인
- `updatedAt`이 갱신되었는지 확인

---

### 📌 Step 5: 메시지 전송 및 AI 응답 생성

```bash
curl -X POST 'http://localhost:9001/chat/messages' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "sessionId": "'$SESSION_ID'",
    "content": "비밀번호를 잊어버렸어요"
  }'
```

**응답 예시:**

```json
{
  "messageId": "46a5fc72-4b1f-48ac-a65b-6bd16f0fcb5f",
  "role": "assistant",
  "content": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다.",
  "createdAt": "2025-12-24T02:00:01Z"
}
```

**환경변수 설정:**

```bash
export MESSAGE_ID="응답에서_받은_messageId"
```

**✅ 확인사항:**

- `messageId`가 UUID 형식인지 확인
- `role`이 `assistant`인지 확인
- `content`에 AI 응답이 포함되어 있는지 확인

**⚠️ 주의사항:**

- AI 서버가 실행 중이어야 함
- RAGFlow 서버가 실행 중이어야 함
- AI 서버 오류 시 에러 응답 반환

---

### 📌 Step 6: 세션별 메시지 목록 조회

```bash
curl -X GET 'http://localhost:9001/chat/sessions/'$SESSION_ID'/messages' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "messages": [
    {
      "id": "3f443176-3d25-45bf-9577-3cd904de56a4",
      "sessionId": "239d0429-b517-4897-beb0-bd1f699999da",
      "role": "user",
      "content": "비밀번호를 잊어버렸어요",
      "tokensIn": null,
      "tokensOut": null,
      "llmModel": null,
      "createdAt": "2025-12-24T02:00:00Z"
    },
    {
      "id": "46a5fc72-4b1f-48ac-a65b-6bd16f0fcb5f",
      "sessionId": "239d0429-b517-4897-beb0-bd1f699999da",
      "role": "assistant",
      "content": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다.",
      "tokensIn": 10,
      "tokensOut": 15,
      "llmModel": "gpt-4",
      "createdAt": "2025-12-24T02:00:01Z"
    }
  ],
  "nextCursor": null,
  "hasNext": false
}
```

**✅ 확인사항:**

- 사용자 메시지와 AI 응답 메시지가 모두 포함되어 있는지 확인
- `role`이 올바르게 구분되어 있는지 확인
- `tokensIn`, `tokensOut`, `llmModel`이 설정되어 있는지 확인

---

### 📌 Step 7: 세션 히스토리 조회

```bash
curl -X GET 'http://localhost:9001/api/chat/sessions/'$SESSION_ID'/history' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "sessionId": "239d0429-b517-4897-beb0-bd1f699999da",
  "title": "수정된 세션 제목",
  "messages": [
    {
      "id": "3f443176-3d25-45bf-9577-3cd904de56a4",
      "sessionId": "239d0429-b517-4897-beb0-bd1f699999da",
      "role": "user",
      "content": "비밀번호를 잊어버렸어요",
      "createdAt": "2025-12-24T02:00:00Z"
    },
    {
      "id": "46a5fc72-4b1f-48ac-a65b-6bd16f0fcb5f",
      "sessionId": "239d0429-b517-4897-beb0-bd1f699999da",
      "role": "assistant",
      "content": "비밀번호 재설정 페이지에서 이메일을 입력하시면 재설정 링크를 보내드립니다.",
      "createdAt": "2025-12-24T02:00:01Z"
    }
  ]
}
```

**✅ 확인사항:**

- 세션 정보와 메시지 목록이 함께 반환되는지 확인
- 메시지가 시간 순서대로 정렬되어 있는지 확인

---

### 📌 Step 8: 메시지 재시도

```bash
curl -X POST 'http://localhost:9001/chat/sessions/'$SESSION_ID'/messages/'$MESSAGE_ID'/retry' \
  -H 'Authorization: Bearer '$TOKEN
```

**응답 예시:**

```json
{
  "id": "46a5fc72-4b1f-48ac-a65b-6bd16f0fcb5f",
  "sessionId": "239d0429-b517-4897-beb0-bd1f699999da",
  "role": "assistant",
  "content": "재시도된 응답 내용",
  "tokensIn": 10,
  "tokensOut": 15,
  "llmModel": "gpt-4",
  "createdAt": "2025-12-24T02:00:01Z"
}
```

**✅ 확인사항:**

- 기존 메시지의 내용이 업데이트되었는지 확인
- AI 서버가 재호출되었는지 확인

**⚠️ 주의사항:**

- `messageId`는 `assistant` 메시지여야 함
- 재시도는 실패한 메시지에 대해서만 의미가 있음

---

### 📌 Step 9: 메시지 피드백 제출

```bash
curl -X POST 'http://localhost:9001/chat/sessions/'$SESSION_ID'/messages/'$MESSAGE_ID'/feedback' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "POSITIVE",
    "comment": "매우 유용합니다."
  }'
```

**응답:** `200 OK` (No Content)

**✅ 확인사항:**

- 피드백이 정상적으로 저장되었는지 확인

**피드백 타입:**

- `POSITIVE`: 긍정적 피드백
- `NEGATIVE`: 부정적 피드백

---

### 📌 Step 10: 세션 피드백 제출

```bash
curl -X POST 'http://localhost:9001/chat/sessions/'$SESSION_ID'/feedback' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "POSITIVE",
    "comment": "세션이 매우 유용했습니다."
  }'
```

**응답:** `200 OK` (No Content)

**✅ 확인사항:**

- 세션 피드백이 정상적으로 저장되었는지 확인

---

### 📌 Step 11: SSE 스트리밍 테스트 (선택적)

**브라우저에서 테스트:**

```bash
# 메시지 전송 후 messageId 획득
curl -X POST 'http://localhost:9001/chat/messages' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "sessionId": "'$SESSION_ID'",
    "content": "스트리밍 테스트 메시지"
  }'

# SSE 스트리밍 (브라우저 또는 EventSource 사용)
# GET /chat/messages/{messageId}/stream
```

**⚠️ 주의사항:**

- SSE는 브라우저에서 직접 테스트하거나 EventSource API를 사용해야 함
- `curl`로는 스트리밍 테스트가 어려움

---

### 📌 Step 12: 세션 삭제 (Soft Delete)

```bash
curl -X DELETE 'http://localhost:9001/api/chat/sessions/'$SESSION_ID \
  -H 'Authorization: Bearer '$TOKEN
```

**응답:** `200 OK` (No Content)

**✅ 확인사항:**

- 세션이 삭제되었는지 확인 (deletedAt 설정)
- 세션 목록 조회 시 삭제된 세션이 제외되는지 확인

---

---

## 개발용 유틸리티 API

### 세션 상태 확인

특정 세션의 상세 정보를 확인할 때 사용:

```bash
curl -X GET 'http://localhost:9001/api/chat/sessions/'$SESSION_ID \
  -H 'Authorization: Bearer '$TOKEN
```

### 메시지 히스토리 확인

세션의 전체 대화 내역을 확인할 때 사용:

```bash
curl -X GET 'http://localhost:9001/api/chat/sessions/'$SESSION_ID'/history' \
  -H 'Authorization: Bearer '$TOKEN
```

---

## 에러 케이스 테스트

### 1. 존재하지 않는 세션 조회

```bash
curl -X GET 'http://localhost:9001/api/chat/sessions/00000000-0000-0000-0000-000000000000' \
  -H 'Authorization: Bearer '$TOKEN
```

**예상 응답:** `404 Not Found`

---

### 2. 잘못된 도메인으로 세션 생성

```bash
curl -X POST 'http://localhost:9001/api/chat/sessions' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "userUuid": "'$USER_UUID'",
    "title": "테스트",
    "domain": "INVALID_DOMAIN"
  }'
```

**예상 응답:** `400 Bad Request`

---

### 3. AI 서버 오류 시나리오

AI 서버가 다운된 상태에서 메시지 전송:

```bash
curl -X POST 'http://localhost:9001/chat/messages' \
  -H 'Authorization: Bearer '$TOKEN \
  -H 'Content-Type: application/json' \
  -d '{
    "sessionId": "'$SESSION_ID'",
    "content": "테스트 메시지"
  }'
```

**예상 응답:** `500 Internal Server Error`

---

## 알려진 문제점 및 참고사항

### ⚠️ 1. AI 서버 의존성

**문제:**

메시지 전송 시 AI 서버가 실행 중이 아니면 오류 발생

**원인:**

- `POST /chat/messages`는 AI 서버(`/ai/chat/messages`)를 호출
- AI 서버가 RAGFlow를 호출하여 검색 수행
- LLM 서비스를 통해 답변 생성

**해결:**

- AI 서버 실행 확인
- RAGFlow 서버 실행 확인
- 네트워크 연결 확인

---

### ⚠️ 2. SSE 스트리밍

현재 SSE 스트리밍은 브라우저에서 직접 테스트해야 함:

| 기능          | 현재 상태        | 설명                          |
| ------------- | ---------------- | ----------------------------- |
| SSE 스트리밍  | 브라우저 필요    | `curl`로는 스트리밍 테스트 어려움 |
| EventSource   | 브라우저 API     | JavaScript EventSource 사용   |

---

### ⚠️ 3. 메시지 재시도

재시도는 실패한 메시지에 대해서만 의미가 있음:

- `assistant` 메시지에 대해서만 재시도 가능
- 재시도 시 기존 메시지 내용이 업데이트됨
- 새로운 메시지가 생성되지 않음

---

## 빠른 테스트 스크립트

전체 플로우를 한 번에 실행하는 스크립트:

```bash
#!/bin/bash

# 환경 변수 설정
TOKEN="YOUR_TOKEN_HERE"
USER_UUID="YOUR_USER_UUID"

# 1. 세션 생성
SESSION_RESPONSE=$(curl -s -X POST 'http://localhost:9001/api/chat/sessions' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "userUuid": "'$USER_UUID'",
    "title": "테스트 세션",
    "domain": "SECURITY"
  }')
SESSION_ID=$(echo $SESSION_RESPONSE | jq -r '.id')
echo "SESSION_ID: $SESSION_ID"

# 2. 메시지 전송
MESSAGE_RESPONSE=$(curl -s -X POST 'http://localhost:9001/chat/messages' \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "sessionId": "'$SESSION_ID'",
    "content": "테스트 메시지"
  }')
MESSAGE_ID=$(echo $MESSAGE_RESPONSE | jq -r '.messageId')
echo "MESSAGE_ID: $MESSAGE_ID"

# 3. 세션 히스토리 조회
curl -s -X GET "http://localhost:9001/api/chat/sessions/$SESSION_ID/history" \
  -H "Authorization: Bearer $TOKEN" | jq

# 4. 메시지 피드백 제출
curl -s -X POST "http://localhost:9001/chat/sessions/$SESSION_ID/messages/$MESSAGE_ID/feedback" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "POSITIVE",
    "comment": "테스트 피드백"
  }'

# 5. 세션 피드백 제출
curl -s -X POST "http://localhost:9001/chat/sessions/$SESSION_ID/feedback" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "POSITIVE",
    "comment": "테스트 세션 피드백"
  }'

echo "테스트 완료!"
```

---

## 체크리스트

- [ ] JWT 토큰 발급 완료
- [ ] userUuid 확인 완료
- [ ] AI 서버 및 RAGFlow 서버 실행 확인
- [ ] Step 1: 세션 생성
- [ ] Step 2: 세션 단건 조회
- [ ] Step 3: 세션 목록 조회
- [ ] Step 4: 세션 수정
- [ ] Step 5: 메시지 전송 및 AI 응답 생성
- [ ] Step 6: 세션별 메시지 목록 조회
- [ ] Step 7: 세션 히스토리 조회
- [ ] Step 8: 메시지 재시도
- [ ] Step 9: 메시지 피드백 제출
- [ ] Step 10: 세션 피드백 제출
- [ ] Step 11: SSE 스트리밍 (브라우저)
- [ ] Step 12: 세션 삭제
- [ ] 에러 케이스 테스트

---

**문서 버전**: 2025-12-24  
**작성자**: AI Assistant

