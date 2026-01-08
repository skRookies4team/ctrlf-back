package com.ctrlf.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 프론트엔드 액션 지시 DTO.
 * AI 서비스에서 반환하는 액션 정보를 프론트엔드로 전달합니다.
 * 예: 교육 영상 이어보기 요청 시 해당 영상을 자동 재생.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatActionDto {
    /**
     * 액션 타입
     * - PLAY_VIDEO: 교육 영상 재생
     * - OPEN_EDU_PANEL: 교육 패널 열기
     * - OPEN_QUIZ: 퀴즈 시작
     */
    @JsonProperty("type")
    private String type;

    /** 교육 ID (PLAY_VIDEO 시 필수) */
    @JsonProperty("education_id")
    private String educationId;

    /** 영상 ID (PLAY_VIDEO 시 필수) */
    @JsonProperty("video_id")
    private String videoId;

    /** 이어보기 시작 위치 (초) */
    @JsonProperty("resume_position_seconds")
    private Integer resumePositionSeconds;

    /** 교육 제목 (UI 표시용) */
    @JsonProperty("education_title")
    private String educationTitle;

    /** 영상 제목 (UI 표시용) */
    @JsonProperty("video_title")
    private String videoTitle;

    /** 현재 진도율 (%) */
    @JsonProperty("progress_percent")
    private Double progressPercent;
}
