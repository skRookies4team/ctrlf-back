## **📘 전처리 + 임베딩 + 영상 스크립트 생성 요청 (Client → Backend)**

## **✔ URL**

**POST /video/material/{materialId}/process**

---

## **✔ 설명**

클라이언트가 업로드한 문서(fileUrl)를 기반으로 AI 서버에 전처리/임베딩/스크립트 생성을 시작하는 API입니다.

- materialId는 DB에 등록된 자료(문서)의 UUID여야 합니다.
- 백엔드는 AI 서버로 요청을 전달하고 수신 상태를 반환합니다.

---

## **📌 Request (클라이언트 → 백엔드)**

### **Path**

- materialId (string, UUID): 자료 ID

**Body**

| key     | 설명        | 타입   | 예시                                   |
| ------- | ----------- | ------ | -------------------------------------- |
| eduId   | 교육 ID     | string | "550e8400-e29b-41d4-a716-446655440000" |
| fileUrl | S3 파일 URL | string | "s3://ctrl-s3/docs/<uuid>.pdf"         |

**Example{  "eduId": "550e8400-e29b-41d4-a716-446655440000",  "fileUrl": "s3://ctrl-s3/docs/abcd-efgh.pdf"}**

---

## **📌 Backend → AI 서버 요청 (내부)**

백엔드는 아래 형태로 AI 서버에 전달합니다.

{

"materialId": "11111111-1111-1111-1111-111111111111",

"eduId": "550e8400-e29b-41d4-a716-446655440000",

"fileUrl": "s3://ctrl-s3/docs/abcd-efgh.pdf"

}

---

## **📌 Response (백엔드 → 클라이언트)**

{

"received": true,

"status": "PROCESSING"

}

- status: 일반적으로 PROCESSING (큐라면 QUEUED일 수 있음)

---

## **📌 Status Codes**

| code               | 의미              |
| ------------------ | ----------------- |
| 200 OK             | 요청 전송 성공    |
| 400 Bad Request    | 요청 데이터 오류  |
| 404 Not Found      | materialId 없음   |
| 500 Internal Error | AI 서버 요청 실패 |

---

## **🔎 비고**

- fileUrl은 Presign 발급 시 받은 값을 그대로 사용합니다.
- 완료 시 AI 서버가 POST /video/script/complete로 콜백을 호출합니다. 프론트는 GET /video/script/{scriptId} 또는 상태 폴링으로 반영하세요.
