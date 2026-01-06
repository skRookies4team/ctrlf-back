## Infra Service - Admin User Management API 명세서

Base URL: http://localhost:9003

---

# 1. Admin User Management APIs

## 1.1 사용자 목록 조회 (검색/필터링)

### ✔ URL

- GET /admin/users/search

### ✔ 설명

- 이름, 사번, 부서, 역할로 필터링하여 사용자 목록을 페이지 형식으로 조회합니다.
- 관리자 대시보드의 계정/롤 관리 페이지에서 사용자 목록을 표시하는 데 사용됩니다.

### ✔ 권한

`ROLE_ADMIN` (또는 Keycloak Admin 권한)

### ✔ 요청

Body: 없음

### Query Parameter

| key        | 설명                     | value 타입 | 옵션     | Nullable | 예시           |
| ---------- | ------------------------ | ---------- | -------- | -------- | -------------- |
| search     | 이름 또는 사번 검색어    | string     | optional | true     | "김민수"       |
| department | 부서 필터                | string     | optional | true     | "개발팀"       |
| role       | 역할 필터                | string     | optional | true     | "SYSTEM_ADMIN" |
| page       | 페이지 번호 (0부터 시작) | number     | optional | false    | 0              |
| size       | 페이지 크기              | number     | optional | false    | 50             |

**department 값 예시:**

- `"총무팀"`, `"기획팀"`, `"마케팅팀"`, `"인사팀"`, `"재무팀"`, `"개발팀"`, `"영업팀"`, `"법무팀"`

**role 값 예시:**

- `"EMPLOYEE"`, `"VIDEO_CREATOR"`, `"CONTENTS_REVIEWER"`, `"COMPLAINT_MANAGER"`, `"SYSTEM_ADMIN"`

### Response

| key   | 설명           | value 타입    | 옵션     | Nullable | 예시         |
| ----- | -------------- | ------------- | -------- | -------- | ------------ |
| items | 사용자 목록    | array(object) | required | false    | 아래 표 참조 |
| page  | 현재 페이지    | number        | required | false    | 0            |
| size  | 페이지 크기    | number        | required | false    | 50           |
| total | 전체 사용자 수 | number        | required | false    | 120          |

items item (사용자 객체)

| key        | 설명               | value 타입    | 옵션     | Nullable | 예시                                   |
| ---------- | ------------------ | ------------- | -------- | -------- | -------------------------------------- |
| id         | Keycloak 사용자 ID | string        | required | false    | "c2f0a1c3-8b4e-4f5a-9d2b-111111111111" |
| username   | 사용자명           | string        | required | false    | "user1"                                |
| email      | 이메일             | string        | optional | true     | "user1@example.com"                    |
| firstName  | 이름               | string        | optional | true     | "민수"                                 |
| lastName   | 성                 | string        | optional | true     | "김"                                   |
| enabled    | 활성화 여부        | boolean       | required | false    | true                                   |
| attributes | 사용자 속성 (Map)  | object        | optional | true     | 아래 attributes 예시 참조              |
| realmRoles | 할당된 역할 목록   | array(string) | optional | true     | ["EMPLOYEE", "SYSTEM_ADMIN"]           |

**attributes 예시:**

```json
{
  "employeeNo": ["2025-01234"],
  "department": ["개발팀"],
  "creatorType": ["DEPT_CREATOR"],
  "creatorDeptScope": ["개발팀", "기획팀"]
}
```

**Response 예시:**

```json
{
  "items": [
    {
      "id": "c2f0a1c3-8b4e-4f5a-9d2b-111111111111",
      "username": "kim.minsu",
      "email": "kim.minsu@example.com",
      "firstName": "민수",
      "lastName": "김",
      "enabled": true,
      "attributes": {
        "employeeNo": ["2025-01234"],
        "department": ["개발팀"]
      },
      "realmRoles": ["EMPLOYEE", "SYSTEM_ADMIN"]
    },
    {
      "id": "d3e1b2d4-9c5f-5g6b-0e3c-222222222222",
      "username": "lee.seoyeon",
      "email": "lee.seoyeon@example.com",
      "firstName": "서연",
      "lastName": "이",
      "enabled": true,
      "attributes": {
        "employeeNo": ["2024-00321"],
        "department": ["인사팀"]
      },
      "realmRoles": ["EMPLOYEE", "COMPLAINT_MANAGER"]
    }
  ],
  "page": 0,
  "size": 50,
  "total": 120
}
```

### Status

| status  | response content |
| ------- | ---------------- |
| 200 OK  | 정상             |
| 401/403 | 인증/권한 오류   |
| 500     | 서버 오류        |

---

## 1.2 사용자 단건 조회

### ✔ URL

- GET /admin/users/{userId}

### ✔ 설명

- 특정 사용자의 상세 정보를 조회합니다.
- 관리자 대시보드에서 사용자를 선택했을 때 상세 정보를 표시하는 데 사용됩니다.
- 역할 정보도 함께 반환됩니다.

### ✔ 권한

`ROLE_ADMIN` (또는 Keycloak Admin 권한)

### ✔ 요청

Body: 없음

### Path Parameter

| key    | 설명               | value 타입 | 옵션     | Nullable | 예시                                   |
| ------ | ------------------ | ---------- | -------- | -------- | -------------------------------------- |
| userId | Keycloak 사용자 ID | string     | required | false    | "c2f0a1c3-8b4e-4f5a-9d2b-111111111111" |

### Response

| key        | 설명               | value 타입    | 옵션     | Nullable | 예시                                   |
| ---------- | ------------------ | ------------- | -------- | -------- | -------------------------------------- |
| id         | Keycloak 사용자 ID | string        | required | false    | "c2f0a1c3-8b4e-4f5a-9d2b-111111111111" |
| username   | 사용자명           | string        | required | false    | "kim.minsu"                            |
| email      | 이메일             | string        | optional | true     | "kim.minsu@example.com"                |
| firstName  | 이름               | string        | optional | true     | "민수"                                 |
| lastName   | 성                 | string        | optional | true     | "김"                                   |
| enabled    | 활성화 여부        | boolean       | required | false    | true                                   |
| attributes | 사용자 속성 (Map)  | object        | optional | true     | 아래 attributes 예시 참조              |
| realmRoles | 할당된 역할 목록   | array(string) | optional | true     | ["EMPLOYEE", "SYSTEM_ADMIN"]           |

**attributes 예시:**

```json
{
  "employeeNo": ["2025-01234"],
  "department": ["개발팀"],
  "creatorType": ["DEPT_CREATOR"],
  "creatorDeptScope": ["개발팀", "기획팀"]
}
```

**Response 예시:**

```json
{
  "id": "c2f0a1c3-8b4e-4f5a-9d2b-111111111111",
  "username": "kim.minsu",
  "email": "kim.minsu@example.com",
  "firstName": "민수",
  "lastName": "김",
  "enabled": true,
  "attributes": {
    "employeeNo": ["2025-01234"],
    "department": ["개발팀"],
    "creatorType": ["DEPT_CREATOR"],
    "creatorDeptScope": ["개발팀", "기획팀"]
  },
  "realmRoles": ["EMPLOYEE", "SYSTEM_ADMIN"]
}
```

### Status

| status  | response content |
| ------- | ---------------- |
| 200 OK  | 정상             |
| 404     | 사용자 없음      |
| 401/403 | 인증/권한 오류   |
| 500     | 서버 오류        |

---

## 1.3 사용 가능한 역할 목록 조회

### ✔ URL

- GET /admin/users/roles

### ✔ 설명

- Keycloak realm에 정의된 모든 역할 목록을 반환합니다.
- 관리자 대시보드에서 역할 체크박스 목록을 표시하는 데 사용됩니다.

### ✔ 권한

`ROLE_ADMIN` (또는 Keycloak Admin 권한)

### ✔ 요청

Body: 없음

### Query Parameter

없음

### Response

| key | 설명      | value 타입    | 옵션     | Nullable | 예시         |
| --- | --------- | ------------- | -------- | -------- | ------------ |
| -   | 역할 목록 | array(object) | required | false    | 아래 표 참조 |

역할 객체

| key         | 설명           | value 타입 | 옵션     | Nullable | 예시        |
| ----------- | -------------- | ---------- | -------- | -------- | ----------- |
| id          | 역할 ID        | string     | required | false    | "EMPLOYEE"  |
| name        | 역할 이름      | string     | required | false    | "EMPLOYEE"  |
| description | 역할 설명      | string     | optional | true     | "기본 역할" |
| composite   | 복합 역할 여부 | boolean    | optional | true     | false       |

**Response 예시:**

```json
[
  {
    "id": "EMPLOYEE",
    "name": "EMPLOYEE",
    "description": "기본 역할",
    "composite": false
  },
  {
    "id": "VIDEO_CREATOR",
    "name": "VIDEO_CREATOR",
    "description": "영상 제작자",
    "composite": false
  },
  {
    "id": "CONTENTS_REVIEWER",
    "name": "CONTENTS_REVIEWER",
    "description": "콘텐츠 검토자",
    "composite": false
  },
  {
    "id": "COMPLAINT_MANAGER",
    "name": "COMPLAINT_MANAGER",
    "description": "신고 관리자",
    "composite": false
  },
  {
    "id": "SYSTEM_ADMIN",
    "name": "SYSTEM_ADMIN",
    "description": "시스템 관리자",
    "composite": false
  }
]
```

### Status

| status  | response content |
| ------- | ---------------- |
| 200 OK  | 정상             |
| 401/403 | 인증/권한 오류   |
| 500     | 서버 오류        |

---

## 1.4 사용자 역할 업데이트

### ✔ URL

- PUT /admin/users/{userId}/roles

### ✔ 설명

- 사용자의 역할을 업데이트합니다.
- 기존 역할을 모두 제거하고 새로운 역할 목록을 할당합니다.
- 관리자 대시보드에서 "저장" 버튼 클릭 시 역할 변경에 사용됩니다.

### ✔ 권한

`ROLE_ADMIN` (또는 Keycloak Admin 권한)

### ✔ 요청

### Path Parameter

| key    | 설명               | value 타입 | 옵션     | Nullable | 예시                                   |
| ------ | ------------------ | ---------- | -------- | -------- | -------------------------------------- |
| userId | Keycloak 사용자 ID | string     | required | false    | "c2f0a1c3-8b4e-4f5a-9d2b-111111111111" |

### Request Body

| key       | 설명             | value 타입    | 옵션     | Nullable | 예시                         |
| --------- | ---------------- | ------------- | -------- | -------- | ---------------------------- |
| roleNames | 할당할 역할 목록 | array(string) | required | false    | ["EMPLOYEE", "SYSTEM_ADMIN"] |

**Request 예시:**

```json
{
  "roleNames": ["EMPLOYEE", "SYSTEM_ADMIN"]
}
```

**빈 배열 전달 시:**

```json
{
  "roleNames": []
}
```

- 모든 역할이 제거됩니다.

### Response

Body: 없음 (204 No Content)

### Status

| status         | response content |
| -------------- | ---------------- |
| 204 No Content | 정상 (성공)      |
| 404            | 사용자 없음      |
| 401/403        | 인증/권한 오류   |
| 500            | 서버 오류        |

---

## 1.5 사용자 정보 업데이트

### ✔ URL

- PUT /admin/users/{userId}

### ✔ 설명

- 사용자 정보를 업데이트합니다.
- `username`, `email`, `firstName`, `lastName`, `enabled`, `attributes` 필드를 수정할 수 있습니다.
- `roleNames`가 제공되면 사용자의 역할도 함께 업데이트됩니다.
- 관리자 대시보드에서 VIDEO_CREATOR 관련 속성(`creatorType`, `creatorDeptScope`)을 저장하는 데 사용됩니다.

### ✔ 권한

`ROLE_ADMIN` (또는 Keycloak Admin 권한)

### ✔ 요청

### Path Parameter

| key    | 설명               | value 타입 | 옵션     | Nullable | 예시                                   |
| ------ | ------------------ | ---------- | -------- | -------- | -------------------------------------- |
| userId | Keycloak 사용자 ID | string     | required | false    | "c2f0a1c3-8b4e-4f5a-9d2b-111111111111" |

### Request Body

| key        | 설명              | value 타입    | 옵션     | Nullable | 예시                          |
| ---------- | ----------------- | ------------- | -------- | -------- | ----------------------------- |
| username   | 사용자명          | string        | optional | true     | "kim.minsu"                   |
| email      | 이메일            | string        | optional | true     | "kim.minsu@example.com"       |
| firstName  | 이름              | string        | optional | true     | "민수"                        |
| lastName   | 성                | string        | optional | true     | "김"                          |
| enabled    | 활성화 여부       | boolean       | optional | true     | true                          |
| attributes | 사용자 속성 (Map) | object        | optional | true     | 아래 attributes 예시 참조     |
| roleNames  | 할당할 역할 목록  | array(string) | optional | true     | ["EMPLOYEE", "VIDEO_CREATOR"] |

**attributes 구조:**

VIDEO_CREATOR 역할과 관련된 속성:

- `creatorType`: 영상 제작자 유형
  - `"DEPT_CREATOR"`: 부서 한정 제작자
  - `"GLOBAL_CREATOR"`: 전사 담당 제작자
  - `null`: VIDEO_CREATOR 역할이 아닌 경우
- `creatorDeptScope`: 제작 가능 부서 목록 (배열)
  - 예: `["개발팀", "기획팀"]`
  - `creatorType`이 `"DEPT_CREATOR"`인 경우 필수
  - `creatorType`이 `"GLOBAL_CREATOR"`인 경우 불필요

**Request 예시 1: 역할과 속성 함께 업데이트**

```json
{
  "roleNames": ["EMPLOYEE", "VIDEO_CREATOR"],
  "attributes": {
    "creatorType": ["DEPT_CREATOR"],
    "creatorDeptScope": ["개발팀", "기획팀"]
  }
}
```

**Request 예시 2: 속성만 업데이트 (역할은 변경하지 않음)**

```json
{
  "attributes": {
    "creatorType": ["GLOBAL_CREATOR"]
  }
}
```

**Request 예시 3: VIDEO_CREATOR 역할 제거 시 속성도 함께 제거**

```json
{
  "roleNames": ["EMPLOYEE"],
  "attributes": {
    "creatorType": [],
    "creatorDeptScope": []
  }
}
```

### Response

Body: 없음 (204 No Content)

### Status

| status         | response content |
| -------------- | ---------------- |
| 204 No Content | 정상 (성공)      |
| 404            | 사용자 없음      |
| 401/403        | 인증/권한 오류   |
| 500            | 서버 오류        |

---

## 주의사항

1. **역할 업데이트**: `PUT /admin/users/{userId}/roles`와 `PUT /admin/users/{userId}` (roleNames 포함) 모두 역할을 업데이트할 수 있습니다. 두 API는 동일한 결과를 반환합니다.

2. **VIDEO_CREATOR 속성**:

   - `creatorType`과 `creatorDeptScope`는 Keycloak의 `attributes` 필드에 저장됩니다.
   - Keycloak attributes는 배열 형식으로 저장되므로, 요청 시에도 배열로 전달해야 합니다.
   - 예: `"creatorType": ["DEPT_CREATOR"]` (단일 값도 배열로)

3. **속성 제거**: 속성을 제거하려면 빈 배열 `[]`을 전달하거나 해당 키를 제거합니다.

4. **부서 필터**: `GET /admin/users/search`의 `department` 파라미터는 사용자의 `attributes.department` 값과 매칭됩니다.

5. **역할 필터**: `GET /admin/users/search`의 `role` 파라미터는 사용자의 `realmRoles` 목록과 매칭됩니다.

---

## 관련 문서

- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/)
- [Education Service API 명세](../education-service/education/api/education_api_spec.md)
