# 📘 **AI 서버 영상 재생성 API**

## ✔ url

**POST /ai/video/job/{jobId}/retry**

---

## ✔ 설명

백엔드가 영상 생성 Job 재시도 요청을

AI 서버로 전달하는 API.

AI 서버는 다음을 수행:

- 기존 jobId 기반으로 영상 재렌더링 시작
- 내부 상태: `PENDING → PROCESSING → COMPLETED / FAILED`
- 실패 시 fail_reason 업데이트
- 완료 시 백엔드로 callback 전달

---

# 📌 **Request (백엔드 → AI 서버)**

| key      | 설명                      | 타입    | 예시     |
| -------- | ------------------------- | ------- | -------- |
| jobId    | Job ID                    | string  | `"uuid"` |
| scriptId | 생성에 사용할 스크립트 ID | string  | `"uuid"` |
| eduId    | 교육 ID                   | string  | `"uuid"` |
| retry    | 재시도 여부               | boolean | `true`   |

### Example

```json
{
  "jobId": "uuid",
  "scriptId": "uuid",
  "eduId": "uuid",
  "retry": true
}
```

---

# 📌 **AI 서버 Response**

```json
{
  "jobId": "uuid",
  "accepted": true,
  "status": "PROCESSING"
}
```

또는 큐 방식이면:

```json
{
  "jobId": "uuid",
  "accepted": true,
  "status": "QUEUED"
}
```

---

# 📌 **Status Codes**

| code               | 의미                      |
| ------------------ | ------------------------- |
| 200 OK             | 재시도 성공               |
| 400 Bad Request    | 요청 데이터 오류          |
| 404 Not Found      | jobId 또는 scriptId 없음  |
| 500 Internal Error | 렌더 파이프라인 실행 불가 |
