## Education Service API (문서 기준)

Base URL: http://localhost:9002

---

# 1. Education

## 1.1 교육 생성 (백엔드 개발용)

### ✔ URL

- POST /edu

### ✔ 설명

- 교육을 생성한다.

### ✔ 권한

`ROLE_ADMIN`

### ✔ 요청

Body: 있음

### Request

| key             | 설명           | value 타입    | 옵션     | Nullable | 예시            |
| --------------- | -------------- | ------------- | -------- | -------- | --------------- |
| title           | 교육 제목      | string        | required | false    | "산업안전 교육" |
| description     | 설명           | string        | optional | true     | "..."           |
| category        | 카테고리       | string        | required | false    | "MANDATORY"     |
| require         | 필수 여부      | boolean       | required | false    | true            |
| passScore       | 통과 점수      | number        | optional | true     | 80              |
| passRatio       | 통과 비율(%)   | number        | optional | true     | 80              |
| departmentScope | 대상 부서 코드 | array(string) | optional | true     | ["HR","DEV"]    |

### Query Parameter

없음

### Response

| key | 설명           | value 타입   | 옵션     | Nullable | 예시      |
| --- | -------------- | ------------ | -------- | -------- | --------- |
| id  | 생성된 교육 ID | string(uuid) | required | false    | "550e..." |

### Example

```json
{ "id": "550e8400-e29b-41d4-a716-446655440000" }
```

### Status

| status          | response content |
| --------------- | ---------------- |
| 201 Created     | 정상 생성        |
| 400 Bad Request | 유효성 실패      |

---

## 1.2 교육 및 영상 목록(내 목록)

### ✔ URL

- GET /edus/me

### ✔ 설명

- 로그인 사용자 기준 이수해야 할 교육 목록과 각 교육에 포함된 영상 목록을 반환합니다.
- 각 교육 객체에 사용자 기준 진행률(progressPercent)과 시청 상태(watchStatus)가 포함되며,
  교육별 `videos` 배열로 영상별 시청률(progressPercent), 시청 상태(watchStatus), 재생 URL(fileUrl) 등을 제공합니다.

### ✔ 권한

`ROLE_USER`

### ✔ 요청

Body: 없음

### Query Parameter

| key       | 설명      | value 타입 | 옵션     | Nullable | 예시      |
| --------- | --------- | ---------- | -------- | -------- | --------- |
| completed | 이수 여부 | boolean    | optional | true     | true      |
| category  | 카테고리  | string     | optional | true     | "JOB"     |
| sort      | 정렬 기준 | string     | optional | true     | "UPDATED" |

### Response

array of object
| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
| --- | --- | --- | --- | --- | --- |
| id | 교육 ID | string(uuid) | required | false | "..." |
| title | 제목 | string | required | false | "..." |
| description | 설명 | string | optional | true | "..." |
| category | 카테고리 | string | required | false | "MANDATORY" |
| required | 필수 여부 | boolean | required | false | true |
| status | 상태(호환 필드) | string | required | false | "IN_PROGRESS" |
| progressPercent | 사용자 기준 교육 진행률(%) | number | required | false | 60 |
| watchStatus | 교육 시청 상태(시청전/시청중/시청완료) | string | required | false | "시청중" |
| targetDepartments | 대상부서 | array(string) | optional | true | ["HR"] |
| videos | 교육에 포함된 영상 목록 | array(object) | required | false | 아래 표 참조 |

videos item

| key               | 설명                              | value 타입   | 옵션     | Nullable | 예시          |
| ----------------- | --------------------------------- | ------------ | -------- | -------- | ------------- |
| id                | 영상 ID                           | string(uuid) | required | false    | "..."         |
| fileUrl           | 영상 파일 URL                     | string       | required | false    | "https://..." |
| duration          | 영상 길이(초)                     | number       | required | false    | 1800          |
| version           | 영상 버전                         | number       | required | false    | 1             |
| isMain            | 메인 영상 여부                    | boolean      | required | false    | false         |
| targetDeptCode    | 대상 부서 코드                    | string       | optional | true     | "DEV"         |
| resumePosition    | 사용자 이어보기 위치(초)          | number       | optional | true     | 120           |
| isCompleted       | 사용자 영상 이수 여부             | boolean      | required | false    | false         |
| totalWatchSeconds | 사용자 누적 시청 시간(초)         | number       | optional | true     | 300           |
| progressPercent   | 진행률(%)                         | number       | required | false    | 65            |
| watchStatus       | 시청 상태(시청전/시청중/시청완료) | string       | required | false    | "시청중"      |

### Status

| status  | response content |
| ------- | ---------------- |
| 200 OK  | 정상             |
| 401/403 | 인증/권한 오류   |

---

## 1.3 교육 상세 (백엔드 개발용)

### ✔ URL

- GET /edu/{id}

### ✔ 설명

- 교육 기본 정보 및 섹션 목록.

### ✔ 권한

`ROLE_USER`

### ✔ 요청

Body: 없음

### Path

- id: UUID

### Response

| key              | 설명        | value 타입    |
| ---------------- | ----------- | ------------- |
| id               | 교육 ID     | string(uuid)  |
| title            | 제목        | string        |
| description      | 설명        | string        |
| duration         | 총 길이(초) | number        |
| sections         | 섹션 목록   | array(object) |
| sections[].id    | 섹션 ID     | string(uuid)  |
| sections[].title | 섹션 제목   | string        |

### Status

| status        | response content |
| ------------- | ---------------- |
| 200 OK        | 정상             |
| 404 Not Found | 없음             |

---

## 1.4 교육 수정 (백엔드 개발용)

### ✔ URL

- PUT /admin/edu/{id}

### ✔ 설명

- 교육 정보를 부분 업데이트.

### ✔ 권한

`ROLE_ADMIN`

### Request

UpdateEducationRequest (모든 필드 optional)
| key | 설명 | 타입 |
| --- | --- | --- |
| title | 제목 | string |
| description | 설명 | string |
| category | 카테고리 | string |
| require | 필수 여부 | boolean |
| passScore | 통과 점수 | number |
| passRatio | 통과 비율 | number |
| departmentScope | 대상 부서 | array(string) |

### Response

| key       | 설명          | 타입         |
| --------- | ------------- | ------------ |
| eduId     | 교육 ID       | string(uuid) |
| updated   | 업데이트 여부 | boolean      |
| updatedAt | ISO-8601      | string       |

### Status

200/400/404

---

## 1.5 교육 삭제 (백엔드 개발용)

### ✔ URL

- DELETE /admin/edu/{id}

### ✔ 설명

- 교육 삭제.

### ✔ 권한

`ROLE_ADMIN`

### Response

| key    | 설명           |
| ------ | -------------- |
| eduId  | 삭제된 교육 ID |
| status | "DELETED"      |

### Status

200/404

---

## 1.6 교육 영상 목록 + 진행 정보 (삭제 예정)

### ✔ URL

- GET /admin/edu/{id}/videos

### ✔ 설명

- 교육의 영상 목록과 사용자 진행 정보(옵션).

### ✔ 권한

`ROLE_USER`

### Response

| key                        | 설명             | 타입          |
| -------------------------- | ---------------- | ------------- |
| id                         | 교육 ID          | string(uuid)  |
| title                      | 제목             | string        |
| videos                     | 영상 목록        | array(object) |
| videos[].id                | 영상 ID          | string(uuid)  |
| videos[].fileUrl           | 재생 URL         | string        |
| videos[].duration          | 길이(초)         | number        |
| videos[].version           | 버전             | number        |
| videos[].isMain            | 메인 여부        | boolean       |
| videos[].targetDeptCode    | 대상 부서        | string        |
| videos[].resumePosition    | 이어보기 위치    | number?       |
| videos[].isCompleted       | 사용자 영상 이수 | boolean?      |
| videos[].totalWatchSeconds | 누적 시청시간    | number?       |

### Status

200/404

---

## 1.7 영상 시청 진행률 업데이트 (유저)

### ✔ URL

- POST /edu/{educationId}/video/{videoId}/progress

### ✔ 권한

`ROLE_USER`

### Request

| key       | 설명              | 타입   |
| --------- | ----------------- | ------ |
| position  | 현재 위치(초)     | number |
| duration  | 영상 길이(초)     | number |
| watchTime | 증가 시청시간(초) | number |

### Response

| key               | 설명                | 타입    |
| ----------------- | ------------------- | ------- |
| updated           | 처리 여부           | boolean |
| progress          | 현재 영상 진행률(%) | number  |
| isCompleted       | 영상 이수 여부      | boolean |
| totalWatchSeconds | 누적 시청 시간      | number  |
| eduProgress       | 교육 전체 진행률(%) | number  |
| eduCompleted      | 교육 이수 여부      | boolean |

### Status

200/400/404

---

## 1.8 교육 이수 처리 (유저)

### ✔ URL

- POST /edu/{id}/complete

### ✔ 권한

`ROLE_USER`

### Response

- { status: "COMPLETED", ... } 혹은 400 상태와 에러 메시지

### Status

200/400
