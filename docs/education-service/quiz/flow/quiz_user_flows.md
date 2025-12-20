# 퀴즈 유저 플로우 분석

## API 목록

1. `GET /quiz/available-educations` - 풀 수 있는 퀴즈 목록 조회
2. `GET /quiz/{eduId}/start` - 퀴즈 시작(문항 생성/복원)
3. `GET /quiz/attempt/{attemptId}/timer` - 타이머 정보 조회
4. `POST /quiz/attempt/{attemptId}/save` - 응답 임시 저장
5. `POST /quiz/attempt/{attemptId}/leave` - 퀴즈 이탈 기록
6. `POST /quiz/attempt/{attemptId}/submit` - 퀴즈 제출/채점
7. `GET /quiz/attempt/{attemptId}/result` - 퀴즈 결과 조회
8. `GET /quiz/{attemptId}/wrongs` - 오답노트 목록 조회
9. `GET /quiz/my-attempts` - 내가 풀었던 퀴즈 응시 내역 조회

---

## 주요 유저 플로우

### 플로우 1: 정상적인 퀴즈 응시 (첫 시도)

```
1. GET /quiz/available-educations
   → 풀 수 있는 퀴즈 목록 조회 (이수 완료한 교육)
   → 응답: 교육 목록 (hasAttempted: false)

2. 사용자가 교육 선택

3. GET /quiz/{eduId}/start
   → 퀴즈 시작 (새 시도 생성)
   → AI 서버에서 문항 생성 (5개)
   → 응답: attemptId, questions[]

4. [반복] GET /quiz/attempt/{attemptId}/timer
   → 타이머 동기화 (주기적 호출, 예: 10초마다)
   → 응답: timeLimit, remainingSeconds, isExpired

5. [반복] POST /quiz/attempt/{attemptId}/save
   → 답안 임시 저장 (사용자가 답 선택 시)
   → 응답: saved, savedCount

6. [선택] POST /quiz/attempt/{attemptId}/leave
   → 이탈 기록 (탭 전환 등 감지 시)
   → 응답: leaveCount, lastLeaveAt

7. POST /quiz/attempt/{attemptId}/submit
   → 퀴즈 제출 및 채점
   → 응답: score, passed, correctCount, wrongCount

8. GET /quiz/attempt/{attemptId}/result
   → 결과 조회 (제출 후 자동 이동 또는 사용자 요청)
   → 응답: score, passed, correctCount, wrongCount

9. [선택] GET /quiz/{attemptId}/wrongs
   → 오답노트 조회
   → 응답: 틀린 문항 목록 (해설 포함, 정답 번호 제외)
```

---

### 플로우 2: 중도 이탈 후 복원하여 완료

```
1. GET /quiz/available-educations
   → 풀 수 있는 퀴즈 목록 조회

2. GET /quiz/{eduId}/start
   → 퀴즈 시작 (새 시도 생성)
   → 응답: attemptId, questions[]

3. POST /quiz/attempt/{attemptId}/save
   → 일부 답안 임시 저장

4. POST /quiz/attempt/{attemptId}/leave
   → 이탈 기록 (페이지 이탈)

5. [나중에] GET /quiz/{eduId}/start
   → 퀴즈 재시작 (기존 미제출 시도 복원)
   → 응답: attemptId (동일), questions[] (저장된 답안 포함)

6. POST /quiz/attempt/{attemptId}/save
   → 나머지 답안 저장

7. POST /quiz/attempt/{attemptId}/submit
   → 제출 및 채점

8. GET /quiz/attempt/{attemptId}/result
   → 결과 조회

9. GET /quiz/{attemptId}/wrongs
   → 오답노트 조회
```

---

### 플로우 3: 재응시 (두 번째 시도)

```
1. GET /quiz/available-educations
   → 풀 수 있는 퀴즈 목록 조회
   → 응답: 교육 목록 (hasAttempted: true, bestScore: 70, passed: false)

2. 사용자가 같은 교육 선택 (재응시)

3. GET /quiz/{eduId}/start
   → 퀴즈 시작 (새 시도 생성, attemptNo: 2)
   → AI 서버에서 새로운 문항 생성

4. [풀이 과정...]

5. POST /quiz/attempt/{attemptId}/submit
   → 제출 및 채점

6. GET /quiz/attempt/{attemptId}/result
   → 결과 조회 (점수 향상 여부 확인)

7. GET /quiz/{attemptId}/wrongs
   → 오답노트 조회
```

---

### 플로우 4: 시간 만료로 자동 제출

```
1. GET /quiz/{eduId}/start
   → 퀴즈 시작

2. [반복] GET /quiz/attempt/{attemptId}/timer
   → 타이머 동기화
   → 응답: remainingSeconds 감소

3. GET /quiz/attempt/{attemptId}/timer
   → 타이머 조회
   → 응답: isExpired: true, remainingSeconds: 0

4. [자동] POST /quiz/attempt/{attemptId}/submit
   → 시간 만료로 자동 제출
   → 응답: score, passed

5. GET /quiz/attempt/{attemptId}/result
   → 결과 조회
```

---

### 플로우 5: 마이페이지에서 응시 내역 조회

```
1. GET /quiz/my-attempts
   → 내가 풀었던 퀴즈 응시 내역 조회
   → 응답: 시도 목록 (점수, 통과 여부, 최고 점수 여부)

2. 사용자가 특정 시도 선택

3. GET /quiz/attempt/{attemptId}/result
   → 결과 상세 조회

4. GET /quiz/{attemptId}/wrongs
   → 오답노트 조회
```

---

### 플로우 6: 타이머 동기화만 (백그라운드)

```
[퀴즈 풀이 화면 활성화 중]

주기적 호출 (예: 10초마다):
GET /quiz/attempt/{attemptId}/timer
→ 타이머 동기화
→ isExpired: true 감지 시 자동 제출 트리거
```

---

## 플로우별 특징

### 플로우 1: 정상 응시

- **시작점**: 퀴즈 목록 화면
- **종료점**: 오답노트 화면
- **특징**: 모든 단계를 순차적으로 진행

### 플로우 2: 중도 이탈 후 복원

- **시작점**: 퀴즈 목록 화면
- **중단점**: 이탈 기록
- **복원점**: 다시 퀴즈 시작 (기존 시도 복원)
- **특징**: `start` API가 미제출 시도를 자동 복원

### 플로우 3: 재응시

- **시작점**: 퀴즈 목록 화면 (이미 응시한 교육)
- **특징**: 새로운 문항 생성 (attemptNo 증가)

### 플로우 4: 시간 만료

- **특징**: 타이머 만료 시 자동 제출
- **트리거**: `timer` API의 `isExpired: true`

### 플로우 5: 마이페이지 조회

- **시작점**: 마이페이지
- **특징**: 과거 응시 내역 조회 및 오답노트 확인

### 플로우 6: 타이머 동기화

- **특징**: 백그라운드에서 주기적 호출
- **목적**: 서버 시간과 동기화, 만료 감지

---

## API 호출 패턴

### 필수 호출 순서

1. `GET /quiz/available-educations` (또는 `GET /quiz/my-attempts`)
2. `GET /quiz/{eduId}/start`
3. `POST /quiz/attempt/{attemptId}/submit`
4. `GET /quiz/attempt/{attemptId}/result`

### 선택적 호출

- `GET /quiz/attempt/{attemptId}/timer` - 타이머 동기화 (주기적)
- `POST /quiz/attempt/{attemptId}/save` - 답안 임시 저장 (답 선택 시)
- `POST /quiz/attempt/{attemptId}/leave` - 이탈 기록 (탭 전환 감지 시)
- `GET /quiz/{attemptId}/wrongs` - 오답노트 조회 (결과 화면에서)

---

## 상태 전이

```
[퀴즈 목록]
    ↓
[퀴즈 시작] → attemptId 생성, questions[] 생성
    ↓
[퀴즈 풀이 중]
    ├─ 답안 임시 저장 (save)
    ├─ 타이머 동기화 (timer)
    ├─ 이탈 기록 (leave)
    └─ 제출 (submit) → submittedAt 설정
    ↓
[결과 화면] → result 조회
    ↓
[오답노트] → wrongs 조회
```

---

## 예외 케이스

### 1. 이미 제출한 시도 재시작 시도

- `GET /quiz/{eduId}/start` 호출
- → 새 시도 생성 (attemptNo 증가)

### 2. 미제출 시도 복원

- `GET /quiz/{eduId}/start` 호출
- → 기존 미제출 시도 자동 복원
- → 저장된 답안 포함하여 반환

### 3. 시간 만료

- `GET /quiz/attempt/{attemptId}/timer` → `isExpired: true`
- → 프론트에서 자동으로 `submit` 호출

### 4. 권한 없는 시도 접근

- 모든 API에서 `userUuid` 검증
- → 403 Forbidden 반환
