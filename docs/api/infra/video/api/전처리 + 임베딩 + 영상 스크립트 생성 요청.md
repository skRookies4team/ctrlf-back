# 📘 **2. 백엔드 → AI 서버: 전처리 + 임베딩 + 스크립트 생성 요청**

## ✔ url

**POST /ai/material/{materialId}/process**

> 이 API는 백엔드에서 AI 서버로 호출하는 내부 API (프론트에서 호출 X)

---

### ✔ 설명

업로드된 교육 자료에 대해 AI서버에서 다음 작업을 **한 번에 수행**:

1. 텍스트 추출
2. 전처리
3. 청크 분리
4. 임베딩 생성
5. 스크립트 자동 생성
6. scriptId 생성

완료 후 **AI 서버가 백엔드로 콜백 전달**한다.

---

# 📌 **AI 서버 Request (백엔드 → AI 서버)**

```json
{
  "materialId": "uuid",
  "eduId": "uuid",
  "fileUrl": "s3://bucket/file.pdf"
}
```

---

# 📌 **AI 서버 Response**

```json
{
  "received": true,
  "status": "PROCESSING"
}
```
