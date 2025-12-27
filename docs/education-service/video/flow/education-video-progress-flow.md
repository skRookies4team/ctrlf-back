# 교육 시청 진행률 및 이수 처리 API 플로우

## 개요
사용자가 교육 영상을 시청하면서 진행률을 업데이트하고, 모든 영상 시청 완료 시 교육 이수 처리를 하는 전체 플로우입니다.

## 주요 엔티티

### 1. EducationVideoProgress
- **용도**: 개별 영상 시청 진행률 관리
- **주요 필드**:
  - `userUuid`: 사용자 UUID
  - `educationId`: 교육 ID
  - `videoId`: 영상 ID
  - `progress`: 진행률 (%)
  - `isCompleted`: 영상 시청 완료 여부 (Boolean)
  - `lastPositionSeconds`: 마지막 재생 위치 (초)
  - `totalWatchSeconds`: 누적 시청 시간 (초)

### 2. EducationProgress
- **용도**: 교육 전체 이수 현황 관리
- **주요 필드**:
  - `userUuid`: 사용자 UUID
  - `educationId`: 교육 ID
  - `progress`: 교육 전체 진행률 (%)
  - `isCompleted`: 교육 이수 완료 여부 (Boolean)
  - `completedAt`: 이수 완료 시각
  - `lastPositionSeconds`: 마지막 재생 위치 (초)
  - `totalWatchSeconds`: 누적 시청 시간 (초)

## API 플로우

### 1단계: 교육 및 영상 목록 조회

#### 1-1. 교육 목록 조회
```
GET /edu/me
Query Parameters:
  - completed: Boolean (선택) - 이수 여부 필터
  - eduType: String (선택) - 교육 유형 필터 (MANDATORY/JOB/ETC)
  - sort: String (선택, 기본값: UPDATED) - 정렬 기준 (UPDATED|TITLE)

Response: List<EducationListItem>
  - 각 교육의 영상 목록 포함
  - 각 영상의 진행률, 시청 상태 포함
  - PUBLISHED 상태의 영상만 반환
```

#### 1-2. 특정 교육의 영상 목록 조회
```
GET /edu/{id}/videos

Response: EducationVideosResponse
  - 교육에 포함된 영상 목록
  - 사용자별 진행 정보 (재생 위치, 누적 시청 시간, 완료 여부 등)
  - PUBLISHED 상태의 영상만 반환
```

### 2단계: 영상 시청 진행률 업데이트 (주기적으로 호출)

#### 2-1. 영상 시청 진행률 업데이트
```
POST /edu/{educationId}/video/{videoId}/progress
Authorization: Bearer {token}

Request Body:
{
  "position": 120,      // 현재 재생 위치 (초)
  "watchTime": 120      // 시청 시간 증가분 (초)
}

**참고**: `duration` 필드는 제거되었습니다. 백엔드에서 `EducationVideo` 엔티티의 `duration` 값을 자동으로 가져옵니다.

Response: VideoProgressResponse
{
  "updated": true,
  "progress": 7,                    // 현재 영상 진행률 (%)
  "isCompleted": false,             // 현재 영상 완료 여부
  "totalWatchSeconds": 120,         // 누적 시청 시간 (초)
  "eduProgress": 40,                // 교육 전체 진행률 (%)
  "eduCompleted": false             // 교육 전체 완료 여부
}
```

**처리 로직**:
1. `EducationVideo` 엔티티 조회 및 검증 (`videoId`, `educationId` 확인)
2. `Education` 엔티티 조회 (`passRatio` 가져오기, 기본값: 100)
3. `EducationVideoProgress` 엔티티 조회 또는 생성
4. 마지막 재생 위치 업데이트 (`lastPositionSeconds`)
5. 누적 시청 시간 업데이트 (`totalWatchSeconds += watchTime`)
6. 진행률 계산: `progress = (position / duration) * 100` (duration은 `EducationVideo`에서 가져옴)
7. 영상 완료 여부 설정: `isCompleted = (progress >= passRatio)` (교육의 `passRatio` 기준)
8. 교육 전체 진행률 계산: **PUBLISHED 상태의 영상들**의 진행률 평균
9. 교육 전체 완료 여부 계산: 모든 **PUBLISHED 영상**이 완료되었는지 확인
10. **자동 교육 이수 처리**: 모든 PUBLISHED 영상이 완료되면 `EducationProgress` 엔티티를 자동으로 완료 처리
    - `EducationProgress` 조회 또는 생성
    - `isCompleted = true`, `completedAt = 현재 시각`, `progress = 100` 설정
    - 저장

**호출 주기**:
- 프론트엔드에서 주기적으로 호출 (예: 5-10초마다)
- 영상 재생 위치가 변경될 때마다 호출
- 영상이 끝났을 때 반드시 호출 (position >= duration)

### 3단계: 교육 이수 처리 (자동 또는 수동)

#### 3-1. 자동 교육 이수 처리 (권장)
**`POST /edu/{educationId}/video/{videoId}/progress` API에서 자동 처리됩니다.**

- 모든 PUBLISHED 영상이 완료되면 자동으로 `EducationProgress` 엔티티가 완료 처리됩니다.
- 별도의 API 호출 없이 진행률 업데이트 API의 응답에서 `eduCompleted: true`를 확인하면 됩니다.

#### 3-2. 수동 교육 이수 처리 (선택적)
```
POST /edu/{id}/complete
Authorization: Bearer {token}

Response: Map<String, Object>
{
  "status": "COMPLETED",              // 또는 "FAILED"
  "completedAt": "2025-12-26T10:00:00Z"
}
```

**실패 응답 예시**:
```json
{
  "status": "FAILED",
  "message": "PUBLISHED 상태의 영상 이수 조건 미충족"
}
```

**처리 로직**:
1. 해당 교육의 **PUBLISHED 상태의 영상 목록** 조회
2. PUBLISHED 영상들의 진행 정보 조회
3. 모든 **PUBLISHED 영상**이 완료 상태인지 검증 (`isCompleted == true`)
4. 조건 충족 시 `EducationProgress` 엔티티 조회 또는 생성
5. 교육 이수 완료 처리:
   - `isCompleted = true`
   - `completedAt = 현재 시각`
   - `progress = 100`
6. 결과 반환 (실패 시: "PUBLISHED 상태의 영상 이수 조건 미충족")

**호출 시점** (선택적):
- 자동 완료 처리가 실패했거나 재시도가 필요한 경우
- 또는 프론트엔드에서 명시적으로 이수 처리를 확인하고 싶을 때

## 전체 플로우 다이어그램

```
[사용자]
    |
    v
[1. 교육 목록 조회]
    GET /edu/me
    |
    v
[2. 영상 목록 조회]
    GET /edu/{id}/videos
    |
    v
[3. 영상 재생 시작]
    |
    v
[4. 주기적으로 진행률 업데이트] ──┐
    POST /edu/{id}/video/{videoId}/progress │
    (5-10초마다, 또는 위치 변경 시)        │
    |                                      │
    v                                      │
[5. 영상 완료 여부 확인]                  │
    (progress >= passRatio)                     │
    |                                      │
    v                                      │
[모든 PUBLISHED 영상 완료?]                         │
    |                                      │
    ├─ No ───────────────────────────────┘
    |
    v
    Yes
    |
    v
[6. 자동 교육 이수 처리]
    (updateVideoProgress API 내부에서 자동 처리)
    EducationProgress.isCompleted = true
    |
    v
[7. 완료!]
    (선택적: POST /edu/{id}/complete로 수동 확인 가능)
```

## 추가 고려사항

### 1. 진행률 계산 방식
- **영상 진행률**: `(현재 재생 위치 / 영상 총 길이) * 100` (최소 0, 최대 100)
  - `duration`은 `EducationVideo` 엔티티에서 가져옴 (요청에서 제거됨)
- **교육 진행률**: `(PUBLISHED 영상들의 진행률 합) / (PUBLISHED 영상 개수)`
- **완료 기준**: 각 영상의 진행률이 `passRatio` 이상 (교육의 `passRatio` 필드 사용, 기본값: 100%)

### 2. passRatio (통과 기준 비율)
- `Education` 엔티티의 `passRatio` 필드 사용
- 기본값: 100%
- 예: `passRatio = 90`이면 영상의 90% 이상 시청 시 완료 처리 가능

### 3. 영상 상태 필터링
- 사용자에게는 **PUBLISHED 상태의 영상만** 노출
- `getEducationsMe()`: PUBLISHED 영상만 조회
- `getEducationVideos()`: PUBLISHED 영상만 조회

### 4. 부서 필터링
- `EducationVideo`의 `departmentScope` 필드로 부서별 접근 제어
- 사용자 부서와 매칭되는 영상만 노출

### 5. 에러 처리
- 영상이 없을 경우: 404 Not Found
- 사용자가 없을 경우: 400 Bad Request
- 권한이 없을 경우: 403 Forbidden

## 구현 확인 사항

### ✅ 구현 완료
- [x] 영상 시청 진행률 업데이트 API (`POST /edu/{id}/video/{videoId}/progress`)
- [x] **자동 교육 이수 처리** (진행률 업데이트 API에서 모든 영상 완료 시 자동 처리)
- [x] 교육 이수 처리 API (`POST /edu/{id}/complete`) - 선택적 수동 호출
- [x] 교육 목록 조회 API (PUBLISHED 영상만 포함)
- [x] 영상 목록 조회 API (PUBLISHED 영상만 포함)
- [x] 진행률 자동 계산 로직
- [x] 교육 전체 진행률 계산 로직

### ⚠️ 주의사항
- 영상 시청 진행률 업데이트는 **주기적으로 호출**해야 함
- 영상이 끝났을 때 (position >= duration) 반드시 호출하여 완료 처리
- **모든 PUBLISHED 영상이 완료되면 자동으로 교육 이수가 처리됨** (`EducationProgress` 자동 업데이트)
- `POST /edu/{id}/complete` API는 선택적으로만 호출 (자동 처리 실패 시 재시도용)

