package com.ctrlf.education.script.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 스크립트 AI 서버 요청/응답 DTO.
 * 
 * <p>API 명세: POST /api/scripts (API_REFERENCE.md)
 */
public final class ScriptAiDtos {
    private ScriptAiDtos() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenerateRequest {
        private String documentId; // 문서 ID (예: "DOC-2025-001")
        private String title; // 스크립트 제목 (예: "개인정보보호 교육")
        private Integer targetDurationSec; // 목표 영상 길이(초) (예: 300)
        private String style; // 스크립트 스타일 (예: "formal", "friendly")
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GenerateResponse {
        private String scriptId; // 생성된 스크립트 ID (예: "SCR-2025-001")
        private String status; // 스크립트 상태 (예: "DRAFT")
        private List<Scene> scenes; // 씬 목록
        private Integer estimatedDurationSec; // 예상 영상 길이(초) (예: 295)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Scene {
        private String sceneId; // 씬 ID
        private Integer sceneOrder; // 씬 순서
        private String purpose; // 목적 (hook, concept, example, summary)
        private String visual; // 시각 요소
        private String narration; // 내레이션
        private String caption; // 자막
        private Integer durationSec; // 씬 길이(초)
        private Map<String, Object> metadata; // 추가 메타데이터 (선택)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ErrorResponse {
        private Boolean success; // false
        private String errorCode; // 에러 코드
        private String message; // 에러 메시지
        private String detail; // 상세 정보
        private String traceId; // 추적 ID
    }
}
