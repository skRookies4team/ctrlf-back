# 📘 **Presigned Upload URL 발급**

### ✔ URL

- **POST /infra/files/presign/upload**

### ✔ 설명

- 클라이언트가 S3에 *직접 업로드*할 수 있도록 **Presigned Upload URL**을 발급해준다.
- 실제 파일 업로드는 서버가 아닌 **프론트 → S3** 로 요청됨.

### ✔ 권한

`ROLE_USER` (필요 시 변경 가능)

---

## **Request Body**

| key         | 설명                         | value 타입 | 옵션     | Nullable | 예시           |
| ----------- | ---------------------------- | ---------- | -------- | -------- | -------------- |
| filename    | 원본 파일명                  | string     | required | false    | `"image.png"`  |
| contentType | MIME 타입                    | string     | required | false    | `"image/png"`  |
| type        | 파일 카테고리(CHAT_IMAGE 등) | string     | required | false    | `"CHAT_IMAGE"` |

### Request Example

```json
{
  "filename": "image.png",
  "contentType": "image/png",
  "type": "CHAT_IMAGE"
}
```

---

## **Response**

| key       | 설명                                       | value 타입 | Nullable | 예시                                          |
| --------- | ------------------------------------------ | ---------- | -------- | --------------------------------------------- |
| uploadUrl | 클라이언트가 PUT 업로드할 S3 Presigned URL | string     | false    | `"https://s3.amazonaws.com/...signed-url..."` |
| fileUrl   | 업로드 완료 후 저장될 S3 key(full path)    | string     | false    | `"s3://bucket/chat/images/abcd.png"`          |

### Response Example

```json
{
  "uploadUrl": "https://s3.amazonaws.com/...signed-url...",
  "fileUrl": "s3://bucket/chat/images/abcd.png"
}
```

---

## **Status**

| status                        | response content                  |
| ----------------------------- | --------------------------------- |
| **200 OK**                    | Presigned URL 반환 성공           |
| **400 Bad Request**           | filename 또는 contentType 값 오류 |
| **401 Unauthorized**          | 인증 실패                         |
| **403 Forbidden**             | 접근 권한 없음                    |
| **500 Internal Server Error** | 서버 내부 오류                    |
