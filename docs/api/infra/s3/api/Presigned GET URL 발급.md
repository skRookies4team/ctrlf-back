# 📘 **Presigned GET URL 발급**

### ✔ URL

- **POST /infra/files/presign/download**

### ✔ 설명

- 백엔드 서비스(예: LLM-service)가 이미지를 읽어 분석하기 위해
  **S3 Presigned GET URL**을 발급한다.
- 파일 접근 제어를 강화하기 위해 서버 간 통신에서 필요.

### ✔ 권한

`ROLE_INTERNAL_SERVICE` (또는 Gateway 인증 기반)

---

## **Request Body**

| key     | 설명                     | value 타입 | 옵션     | Nullable | 예시                                 |
| ------- | ------------------------ | ---------- | -------- | -------- | ------------------------------------ |
| fileUrl | S3 object key(full path) | string     | required | false    | `"s3://bucket/chat/images/abcd.png"` |

### Request Example

```json
{
  "fileUrl": "s3://bucket/chat/images/abcd.png"
}
```

---

## **Response**

| key         | 설명           | value 타입 | Nullable | 예시                                              |
| ----------- | -------------- | ---------- | -------- | ------------------------------------------------- |
| downloadUrl | 사인드 GET URL | string     | false    | `"https://s3.amazonaws.com/...signed-get-url..."` |

### Response Example

```json
{
  "downloadUrl": "https://s3.amazonaws.com/...signed-get-url..."
}
```

---

## **Status**

| status                        | response content                        |
| ----------------------------- | --------------------------------------- |
| **200 OK**                    | Presigned GET URL 생성 성공             |
| **400 Bad Request**           | fileUrl이 잘못되었거나 존재하지 않을 때 |
| **401 Unauthorized**          | 인증 실패                               |
| **403 Forbidden**             | 권한 부족                               |
| **404 Not Found**             | 파일을 찾지 못함                        |
| **500 Internal Server Error** | 서버 에러                               |
