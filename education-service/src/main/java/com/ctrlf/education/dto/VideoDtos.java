package com.ctrlf.education.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 영상 생성 관련 API의 요청/응답 DTO 모음.
 */
public final class VideoDtos {

    private VideoDtos() {}

    // ========================
    // 스크립트 관련 DTOs
    // ========================

    /**
     * 스크립트 조회 응답.
     */
    @Schema(description = "스크립트 조회 응답")
    public record ScriptResponse(
        @Schema(description = "스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID scriptId,

        @Schema(description = "교육 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID educationId,

        @Schema(description = "자료 ID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID materialId,

        @Schema(description = "스크립트 내용", example = "교육 영상 스크립트 내용...")
        String script,

        @Schema(description = "스크립트 버전", example = "1")
        Integer version
    ) {}

    /**
     * 스크립트 수정 요청.
     */
    @Schema(description = "스크립트 수정 요청")
    public record ScriptUpdateRequest(
        @Schema(description = "수정할 스크립트 내용", example = "관리자가 수정한 스크립트...")
        @NotBlank(message = "스크립트 내용은 필수입니다")
        String script
    ) {}

    /**
     * 스크립트 수정 응답.
     */
    @Schema(description = "스크립트 수정 응답")
    public record ScriptUpdateResponse(
        @Schema(description = "수정 성공 여부", example = "true")
        boolean updated,

        @Schema(description = "스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID scriptId
    ) {}

    /**
     * AI 서버 → 백엔드: 스크립트 생성 완료 콜백 요청.
     */
    @Schema(description = "스크립트 생성 완료 콜백 요청 (AI 서버 → 백엔드)")
    public record ScriptCompleteCallback(
        @Schema(description = "자료 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "materialId는 필수입니다")
        UUID materialId,

        @Schema(description = "생성된 스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        @NotNull(message = "scriptId는 필수입니다")
        UUID scriptId,

        @Schema(description = "LLM이 자동 생성한 스크립트", example = "생성된 교육 스크립트...")
        @NotBlank(message = "script는 필수입니다")
        String script,

        @Schema(description = "스크립트 버전 번호", example = "1")
        @NotNull(message = "version은 필수입니다")
        Integer version
    ) {}

    /**
     * 스크립트 생성 완료 콜백 응답.
     */
    @Schema(description = "스크립트 생성 완료 콜백 응답")
    public record ScriptCompleteResponse(
        @Schema(description = "저장 성공 여부", example = "true")
        boolean saved,

        @Schema(description = "스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID scriptId
    ) {}

    // ========================
    // 영상 생성 Job 관련 DTOs
    // ========================

    /**
     * 영상 생성 요청.
     */
    @Schema(description = "영상 생성 요청")
    public record VideoJobRequest(
        @Schema(description = "교육 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "eduId는 필수입니다")
        UUID eduId,

        @Schema(description = "최종 스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        @NotNull(message = "scriptId는 필수입니다")
        UUID scriptId
    ) {}

    /**
     * 영상 생성 요청 응답.
     */
    @Schema(description = "영상 생성 요청 응답")
    public record VideoJobResponse(
        @Schema(description = "생성된 Job ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID jobId,

        @Schema(description = "Job 상태", example = "QUEUED")
        String status
    ) {}

    /**
     * 영상 재시도 응답.
     */
    @Schema(description = "영상 재시도 응답")
    public record VideoRetryResponse(
        @Schema(description = "Job ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID jobId,

        @Schema(description = "재시도 후 상태", example = "QUEUED")
        String status,

        @Schema(description = "누적 재시도 횟수", example = "2")
        Integer retryCount
    ) {}

    /**
     * AI 서버 → 백엔드: 영상 생성 완료 콜백 요청.
     */
    @Schema(description = "영상 생성 완료 콜백 요청 (AI 서버 → 백엔드)")
    public record VideoCompleteCallback(
        @Schema(description = "Job ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "jobId는 필수입니다")
        UUID jobId,

        @Schema(description = "생성된 영상 URL", example = "https://cdn.com/video.mp4")
        String videoUrl,

        @Schema(description = "영상 길이(초)", example = "1230")
        Integer duration,

        @Schema(description = "완료 상태", example = "COMPLETED")
        @NotBlank(message = "status는 필수입니다")
        String status
    ) {}

    /**
     * 영상 생성 완료 콜백 응답.
     */
    @Schema(description = "영상 생성 완료 콜백 응답")
    public record VideoCompleteResponse(
        @Schema(description = "저장 성공 여부", example = "true")
        boolean saved
    ) {}

    // ========================
    // 백엔드 → AI 서버 요청용 내부 DTOs
    // ========================

    /**
     * 클라이언트 → 백엔드: 전처리/임베딩/스크립트 생성 시작 요청.
     * materialId는 PathVariable로 받고, 바디에는 eduId와 fileUrl만 받습니다.
     */
    @Schema(description = "전처리/임베딩/스크립트 생성 시작 요청 (클라이언트 → 백엔드)")
    public record MaterialProcessStartRequest(
        @Schema(description = "교육 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "eduId는 필수입니다")
        UUID eduId,

        @Schema(description = "S3 파일 URL", example = "s3://bucket/docs/file.pdf")
        @NotBlank(message = "fileUrl은 필수입니다")
        String fileUrl
    ) {}

    /**
     * 백엔드 → AI 서버: 전처리 + 임베딩 + 스크립트 생성 요청.
     */
    @Schema(description = "전처리/임베딩/스크립트 생성 요청 (백엔드 → AI 서버)")
    public record MaterialProcessRequest(
        @Schema(description = "자료 ID")
        UUID materialId,

        @Schema(description = "교육 ID")
        UUID eduId,

        @Schema(description = "파일 URL (S3)")
        String fileUrl
    ) {}

    /**
     * AI 서버 응답: 처리 요청 수신 확인.
     */
    @Schema(description = "AI 서버 처리 요청 응답")
    public record AiProcessResponse(
        @Schema(description = "요청 수신 여부", example = "true")
        boolean received,

        @Schema(description = "처리 상태", example = "PROCESSING")
        String status
    ) {}

    /**
     * 백엔드 → AI 서버: 영상 생성 시작 요청.
     */
    @Schema(description = "영상 생성 시작 요청 (백엔드 → AI 서버)")
    public record VideoStartRequest(
        @Schema(description = "스크립트 ID")
        UUID scriptId,

        @Schema(description = "교육 ID")
        UUID eduId
    ) {}

    /**
     * 백엔드 → AI 서버: 영상 재생성 요청.
     */
    @Schema(description = "영상 재생성 요청 (백엔드 → AI 서버)")
    public record VideoRetryRequest(
        @Schema(description = "Job ID")
        UUID jobId,

        @Schema(description = "스크립트 ID")
        UUID scriptId,

        @Schema(description = "교육 ID")
        UUID eduId,

        @Schema(description = "재시도 여부")
        boolean retry
    ) {}

    /**
     * AI 서버 응답: 영상 생성/재생성 요청 수신 확인.
     */
    @Schema(description = "AI 서버 영상 요청 응답")
    public record AiVideoResponse(
        @Schema(description = "Job ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID jobId,

        @Schema(description = "요청 수락 여부", example = "true")
        boolean accepted,

        @Schema(description = "상태", example = "QUEUED")
        String status
    ) {}
}
