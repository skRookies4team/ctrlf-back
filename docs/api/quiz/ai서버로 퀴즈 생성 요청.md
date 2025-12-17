이 API는 내부 백엔드/관리자용입니다.
백엔드(ctrlf-back)에서 교육 문서의 퀴즈 후보 블록들을 보내면,
AI가 객관식 문제/보기/정답/해설을 생성하여 반환합니다.

## 주요 기능

1. **1차 응시**: 새로운 퀴즈 세트 생성 (attempt_no=1)
2. **2차 응시**: 기존 문항 중복 방지 (attempt_no=2, exclude_previous_questions 사용)

## 요청 예시

```json
{
  "educationId": "EDU-SEC-2025-001",
  "docId": "DOC-SEC-001",
  "docVersion": "v1",
  "attemptNo": 1,
  "language": "ko",
  "numQuestions": 10,
  "difficultyDistribution": {
    "easy": 5,
    "normal": 3,
    "hard": 2
  },
  "questionType": "MCQ_SINGLE",
  "maxOptions": 4,
  "quizCandidateBlocks": [
    {
      "blockId": "BLOCK-001",
      "chapterId": "CH1",
      "learningObjectiveId": "LO-1",
      "text": "USB 메모리를 사외로 반출할 때에는 정보보호팀의 사전 승인을 받아야 한다.",
      "tags": ["USB", "반출", "승인"]
    }
  ],
  "excludePreviousQuestions": []
}
```

## 응답 예시

```json
{
  "educationId": "EDU-SEC-2025-001",
  "docId": "DOC-SEC-001",
  "docVersion": "v1",
  "attemptNo": 1,
  "generatedCount": 10,
  "questions": [
    {
      "questionId": "Q-20251212-ABCD1234",
      "status": "DRAFT_AI_GENERATED",
      "questionType": "MCQ_SINGLE",
      "stem": "USB 메모리를 사외로 반출할 때 필요한 조치는?",
      "options": [
        { "optionId": "OPT-1", "text": "정보보호팀의 사전 승인", "isCorrect": true },
        { "optionId": "OPT-2", "text": "팀장에게 구두 보고", "isCorrect": false },
        { "optionId": "OPT-3", "text": "자유롭게 반출", "isCorrect": false },
        { "optionId": "OPT-4", "text": "사후 보고", "isCorrect": false }
      ],
      "difficulty": "EASY",
      "explanation": "USB 반출 시에는 반드시 정보보호팀의 사전 승인을 받아야 합니다."
    }
  ]
}
```

## TODO: 인증/권한

- 이 엔드포인트는 내부 백엔드/관리자 전용입니다.
- IP 제한 또는 헤더 토큰 기반 인증을 추가할 예정입니다.

## TODO: Phase 17 예정

- LLM Self-check 기반 고급 QC 파이프라인
- RAG 재검증을 통한 정답 검증
- 문장 유사도(embedding) 기반 중복 제거
