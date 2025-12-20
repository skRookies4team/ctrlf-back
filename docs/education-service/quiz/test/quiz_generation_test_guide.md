# 퀴즈 생성 테스트 가이드

퀴즈 생성 기능을 테스트하기 위한 단계별 가이드입니다.

## 사전 준비 사항

### 1. 데이터베이스 준비

#### 1.1 교육 데이터 삽입

```sql
-- 1. Education 테이블에 교육 데이터 삽입
INSERT INTO education (id, title, description, pass_score, created_at, updated_at, deleted_at)
VALUES (
    '550e8400-e29b-41d4-a716-446655440000',  -- education_id (UUID)
    '정보보안 기초 교육',
    '정보보안의 기본 개념을 학습합니다.',
    70,  -- 통과 점수
    NOW(),
    NOW(),
    NULL
);

-- 2. EducationScript 테이블에 스크립트 데이터 삽입
INSERT INTO education_script (
    id,
    education_id,
    version,
    source_doc_id,
    created_at,
    updated_at,
    deleted_at
)
VALUES (
    '660e8400-e29b-41d4-a716-446655440000',  -- script_id (UUID)
    '550e8400-e29b-41d4-a716-446655440000',  -- education_id
    1,  -- version
    '770e8400-e29b-41d4-a716-446655440000',  -- source_doc_id (RAG 문서 ID)
    NOW(),
    NOW(),
    NULL
);

-- 3. EducationScriptScene 테이블에 씬 데이터 삽입 (퀴즈 생성에 사용될 텍스트)
INSERT INTO education_script_scene (
    id,
    script_id,
    chapter_id,
    scene_index,
    narration,  -- 퀴즈 생성에 사용될 텍스트 (우선순위 1)
    caption,    -- narration이 없으면 사용 (우선순위 2)
    visual,     -- caption도 없으면 사용 (우선순위 3)
    created_at,
    updated_at,
    deleted_at
)
VALUES
-- 씬 1
(
    '880e8400-e29b-41d4-a716-446655440001',
    '660e8400-e29b-41d4-a716-446655440000',
    'CH1',
    1,
    '정보보안의 핵심은 기밀성, 무결성, 가용성의 세 가지 원칙입니다. 기밀성은 인가된 사용자만 정보에 접근할 수 있도록 하는 것입니다.',
    NULL,
    NULL,
    NOW(),
    NOW(),
    NULL
),
-- 씬 2
(
    '880e8400-e29b-41d4-a716-446655440002',
    '660e8400-e29b-41d4-a716-446655440000',
    'CH1',
    2,
    '비밀번호는 최소 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다. 또한 정기적으로 변경하는 것이 좋습니다.',
    NULL,
    NULL,
    NOW(),
    NOW(),
    NULL
),
-- 씬 3
(
    '880e8400-e29b-41d4-a716-446655440003',
    '660e8400-e29b-41d4-a716-446655440000',
    'CH2',
    1,
    '피싱 공격은 이메일이나 메시지를 통해 개인정보를 탈취하려는 공격입니다. 의심스러운 링크를 클릭하지 않는 것이 중요합니다.',
    NULL,
    NULL,
    NOW(),
    NOW(),
    NULL
),
-- 씬 4
(
    '880e8400-e29b-41d4-a716-446655440004',
    '660e8400-e29b-41d4-a716-446655440000',
    'CH2',
    2,
    'USB 메모리를 사외로 반출할 때에는 정보보호팀의 사전 승인을 받아야 합니다. 승인 없이 반출하는 것은 보안 위반입니다.',
    NULL,
    NULL,
    NOW(),
    NOW(),
    NULL
),
-- 씬 5
(
    '880e8400-e29b-41d4-a716-446655440005',
    '660e8400-e29b-41d4-a716-446655440000',
    'CH3',
    1,
    '소셜 엔지니어링은 사람의 심리를 이용하여 정보를 얻는 공격입니다. 항상 신원을 확인하고 의심스러운 요청은 거부해야 합니다.',
    NULL,
    NULL,
    NOW(),
    NOW(),
    NULL
);
```

#### 1.2 교육 이수 완료 처리

```sql
-- EducationProgress 테이블에 이수 완료 데이터 삽입
-- (퀴즈는 이수 완료한 교육만 풀 수 있음)
INSERT INTO education_progress (
    id,
    user_uuid,
    education_id,
    is_completed,
    completed_at,
    progress,
    updated_at
)
VALUES (
    '990e8400-e29b-41d4-a716-446655440000',  -- progress_id (UUID)
    'aa0e8400-e29b-41d4-a716-446655440000',  -- user_uuid (테스트용 사용자 UUID)
    '550e8400-e29b-41d4-a716-446655440000',  -- education_id
    true,  -- is_completed: true (이수 완료)
    NOW(),  -- completed_at
    100,    -- progress: 100%
    NOW()
);
```

### 2. AI 서버 실행 확인

```bash
# AI 서버가 실행 중인지 확인
curl http://localhost:8000/health

# 응답 예시:
# {"status":"ok","app":"ctrlf-ai-gateway","version":"0.1.0","env":"dev"}

# AI 서버가 실행되지 않은 경우
cd ctrlf-ai
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 3. 백엔드 서버 실행 확인

```bash
# education-service가 실행 중인지 확인
# application.yml에서 AI 서버 URL 확인:
# app.quiz.ai.base-url: http://localhost:8000
```

### 4. JWT 토큰 발급

Keycloak에서 테스트용 사용자로 로그인하여 JWT 토큰을 발급받습니다.

```bash
# Keycloak에서 토큰 발급 (예시)
TOKEN=$(curl -X POST http://localhost:8080/realms/ctrlf/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=ctrlf-backend" \
  -d "username=testuser" \
  -d "password=testpass" \
  -d "grant_type=password" | jq -r '.access_token')

echo $TOKEN
```

---

## 테스트 플로우

### 단계 1: 풀 수 있는 퀴즈 목록 조회

```bash
curl -X GET "http://localhost:8080/quiz/available-educations" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**기대 응답:**

```json
[
  {
    "educationId": "550e8400-e29b-41d4-a716-446655440000",
    "title": "정보보안 기초 교육",
    "hasAttempted": false,
    "bestScore": null,
    "passed": false
  }
]
```

### 단계 2: 퀴즈 시작 (AI 서버로 퀴즈 생성 요청)

```bash
EDUCATION_ID="550e8400-e29b-41d4-a716-446655440000"

curl -X GET "http://localhost:8080/quiz/${EDUCATION_ID}/start" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -v
```

**기대 응답:**

```json
{
  "attemptId": "bb0e8400-e29b-41d4-a716-446655440000",
  "questions": [
    {
      "questionId": "cc0e8400-e29b-41d4-a716-446655440001",
      "question": "정보보안의 핵심 원칙은 무엇인가요?",
      "choices": [
        "기밀성, 무결성, 가용성",
        "기밀성, 무결성, 신뢰성",
        "기밀성, 가용성, 신뢰성",
        "무결성, 가용성, 신뢰성"
      ],
      "userSelectedIndex": null
    },
    {
      "questionId": "cc0e8400-e29b-41d4-a716-446655440002",
      "question": "비밀번호는 최소 몇 자 이상이어야 하나요?",
      "choices": ["6자", "8자", "10자", "12자"],
      "userSelectedIndex": null
    }
    // ... 총 5개 문항
  ]
}
```

**백엔드 로그 확인:**

- `QuizService.start()` 메서드 실행
- `QuizAiClient.generate()` 호출
- AI 서버로 요청 전송:
  ```json
  {
    "language": "ko",
    "numQuestions": 5,
    "maxOptions": 4,
    "quizCandidateBlocks": [
      {
        "blockId": "880e8400-e29b-41d4-a716-446655440001",
        "docId": "770e8400-e29b-41d4-a716-446655440000",
        "docVersion": "v1",
        "chapterId": "CH1",
        "learningObjectiveId": null,
        "text": "정보보안의 핵심은 기밀성, 무결성, 가용성의 세 가지 원칙입니다...",
        "tags": [],
        "articlePath": null
      }
      // ... 다른 블록들
    ],
    "excludePreviousQuestions": []
  }
  ```

**AI 서버 로그 확인:**

- `/ai/quiz/generate` 엔드포인트 호출
- LLM으로 퀴즈 생성
- 응답 반환:
  ```json
  {
    "generatedCount": 5,
    "questions": [
      {
        "questionId": "Q-20251218-ABCD1234",
        "status": "DRAFT_AI_GENERATED",
        "questionType": "MCQ_SINGLE",
        "stem": "정보보안의 핵심 원칙은 무엇인가요?",
        "options": [
          { "optionId": "OPT-1", "text": "기밀성, 무결성, 가용성", "isCorrect": true },
          { "optionId": "OPT-2", "text": "기밀성, 무결성, 신뢰성", "isCorrect": false },
          { "optionId": "OPT-3", "text": "기밀성, 가용성, 신뢰성", "isCorrect": false },
          { "optionId": "OPT-4", "text": "무결성, 가용성, 신뢰성", "isCorrect": false }
        ],
        "difficulty": "EASY",
        "explanation": "정보보안의 핵심 원칙은 기밀성, 무결성, 가용성입니다."
      }
      // ... 다른 문항들
    ]
  }
  ```

### 단계 3: 생성된 문항 확인 (DB 조회)

```sql
-- QuizAttempt 확인
SELECT id, user_uuid, education_id, attempt_no, time_limit, created_at, submitted_at
FROM quiz_attempt
WHERE education_id = '550e8400-e29b-41d4-a716-446655440000'
ORDER BY created_at DESC
LIMIT 1;

-- QuizQuestion 확인
SELECT id, attempt_id, question, options, correct_option_idx, explanation
FROM quiz_question
WHERE attempt_id = '<위에서 조회한 attempt_id>'
ORDER BY created_at;
```

### 단계 4: 타이머 정보 확인

```bash
ATTEMPT_ID="bb0e8400-e29b-41d4-a716-446655440000"

curl -X GET "http://localhost:8080/quiz/attempt/${ATTEMPT_ID}/timer" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**기대 응답:**

```json
{
  "timeLimit": 900,
  "remainingSeconds": 900,
  "isExpired": false,
  "startedAt": "2025-12-18T10:00:00Z"
}
```

---

## 문제 해결

### 1. AI 서버 연결 실패

**증상:**

```
Connection refused 또는 500 Internal Server Error
```

**해결:**

- AI 서버가 실행 중인지 확인: `curl http://localhost:8000/health`
- `application.yml`의 `app.quiz.ai.base-url` 설정 확인
- AI 서버 로그 확인

### 2. 퀴즈 생성 실패 (Placeholder 문항 반환)

**증상:**

- 응답에 "Placeholder Question 1", "Placeholder Question 2" 등이 반환됨

**원인:**

- AI 서버 연결 실패
- LLM 응답 파싱 실패
- `quizCandidateBlocks`가 비어있음

**해결:**

- AI 서버 로그 확인
- `EducationScriptScene` 데이터 확인 (narration, caption, visual 중 하나는 있어야 함)
- AI 서버의 `/ai/quiz/generate` 엔드포인트 직접 테스트

### 3. "education not found" 오류

**원인:**

- `Education` 테이블에 해당 교육이 없음
- `deleted_at`이 설정되어 있음

**해결:**

- DB에서 교육 데이터 확인
- `deleted_at IS NULL` 조건 확인

### 4. "풀 수 있는 퀴즈 목록이 비어있음"

**원인:**

- `EducationProgress.is_completed = true`인 레코드가 없음
- `user_uuid`가 일치하지 않음

**해결:**

- `EducationProgress` 테이블 확인
- JWT 토큰의 `sub` 필드와 `user_uuid` 일치 여부 확인

---

## 재응시 테스트 (2차 시도)

### 1. 첫 번째 시도 제출

```bash
ATTEMPT_ID="bb0e8400-e29b-41d4-a716-446655440000"

curl -X POST "http://localhost:8080/quiz/attempt/${ATTEMPT_ID}/submit" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "answers": [
      {"questionId": "cc0e8400-e29b-41d4-a716-446655440001", "userSelectedIndex": 0},
      {"questionId": "cc0e8400-e29b-41d4-a716-446655440002", "userSelectedIndex": 1}
    ]
  }'
```

### 2. 두 번째 시도 시작 (재응시)

```bash
EDUCATION_ID="550e8400-e29b-41d4-a716-446655440000"

curl -X GET "http://localhost:8080/quiz/${EDUCATION_ID}/start" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**확인 사항:**

- `attemptNo: 2`로 증가
- `excludePreviousQuestions`에 첫 번째 시도의 문항들이 포함됨
- AI 서버로 전송되는 요청에 `excludePreviousQuestions` 필드 확인

---

## 참고 사항

1. **AI 서버 난이도 분배**: AI 서버가 내부에서 고정 비율로 결정 (쉬움 50%, 보통 30%, 어려움 20%)
2. **시간 제한**: 새 시도 생성 시 자동으로 15분(900초) 설정
3. **문항 수**: 기본값 5개 (코드에서 `req.setNumQuestions(5)` 설정)
4. **보기 개수**: 기본값 4개 (코드에서 `req.setMaxOptions(4)` 설정)
