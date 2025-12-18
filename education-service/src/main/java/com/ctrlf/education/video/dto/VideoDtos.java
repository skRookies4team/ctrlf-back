package com.ctrlf.education.video.dto;

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
        UUID scriptId,

        @Schema(description = "영상 컨텐츠 ID", example = "550e8400-e29b-41d4-a716-446655440002")
        @NotNull(message = "videoId는 필수입니다")
        UUID videoId
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

    // ---------- Video Job management (list/detail/update) ----------
    @Schema(description = "영상 생성 Job 요약")
    public record JobItem(
        @Schema(description = "Job ID") UUID jobId,
        @Schema(description = "스크립트 ID") UUID scriptId,
        @Schema(description = "교육 ID") UUID eduId,
        @Schema(description = "상태") String status,
        @Schema(description = "재시도 횟수") Integer retryCount,
        @Schema(description = "생성된 영상 URL") String videoUrl,
        @Schema(description = "영상 길이(초)") Integer duration,
        @Schema(description = "생성 시각 (ISO8601)") String createdAt,
        @Schema(description = "수정 시각 (ISO8601)") String updatedAt,
        @Schema(description = "실패 사유") String failReason
    ) {}

    @Schema(description = "영상 생성 Job 수정 요청")
    public record VideoJobUpdateRequest(
        @Schema(description = "새 상태", example = "CANCELLED") String status,
        @Schema(description = "실패 사유 또는 메모", example = "수동 취소/오류 메시지 등") String failReason,
        @Schema(description = "생성된 영상 URL(수정 필요시)", example = "https://cdn.example.com/video.mp4") String videoUrl,
        @Schema(description = "영상 길이(초)", example = "120") Integer duration
    ) {}

    // ========================
    // 영상 메타 CRUD DTOs (ADMIN)
    // ========================

    @Schema(description = "영상 메타 정보")
    public record VideoMetaItem(
        @Schema(description = "영상 ID") UUID id,
        @Schema(description = "교육 ID") UUID educationId,
        @Schema(description = "영상 제목") String title,
        @Schema(description = "생성 Job ID") UUID generationJobId,
        @Schema(description = "파일 URL") String fileUrl,
        @Schema(description = "버전") Integer version,
        @Schema(description = "길이(초)") Integer duration,
        @Schema(description = "상태") String status,
        @Schema(description = "대상 부서 코드") String targetDeptCode,
        @Schema(description = "수강 가능 부서(JSON)") String departmentScope,
        @Schema(description = "재생 순서(0-base)") Integer orderIndex,
        @Schema(description = "생성시각 ISO8601") String createdAt
    ) {}

    @Schema(description = "영상 메타 수정 요청 (부분 업데이트)")
    public record VideoMetaUpdateRequest(
        @Schema(description = "영상 제목") String title,
        @Schema(description = "파일 URL") String fileUrl,
        @Schema(description = "버전") Integer version,
        @Schema(description = "길이(초)") Integer duration,
        @Schema(description = "상태") String status,
        @Schema(description = "대상 부서 코드") String targetDeptCode,
        @Schema(description = "수강 가능 부서(JSON)") String departmentScope,
        @Schema(description = "재생 순서(0-base)") Integer orderIndex
    ) {}

    // ========================
    // 영상 컨텐츠 관리 DTOs (ADMIN)
    // ========================

    @Schema(description = "영상 컨텐츠 생성 요청")
    public record VideoCreateRequest(
        @Schema(description = "교육 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "educationId는 필수입니다")
        UUID educationId,

        @Schema(description = "영상 제목", example = "2024년 성희롱 예방 교육")
        @NotBlank(message = "title은 필수입니다")
        String title,

        @Schema(description = "수강 가능 부서(JSON)", example = "[\"HR\", \"IT\"]")
        String departmentScope
    ) {}

    @Schema(description = "영상 컨텐츠 생성 응답")
    public record VideoCreateResponse(
        @Schema(description = "생성된 영상 ID") UUID videoId,
        @Schema(description = "상태") String status
    ) {}

    @Schema(description = "영상 상태 변경 응답")
    public record VideoStatusResponse(
        @Schema(description = "영상 ID") UUID videoId,
        @Schema(description = "이전 상태") String previousStatus,
        @Schema(description = "현재 상태") String currentStatus,
        @Schema(description = "변경 시각") String updatedAt
    ) {}

    @Schema(description = "검토 반려 요청")
    public record VideoRejectRequest(
        @Schema(description = "반려 사유", example = "스크립트 내용 수정 필요")
        String reason
    ) {}

    // ========================
    // 영상 상태 Enum (어드민 테스트용)
    // ========================

    @Schema(description = "영상 상태")
    public enum VideoStatus {
        DRAFT,                      // 초기 생성
        SCRIPT_GENERATING,          // 스크립트 생성 중
        SCRIPT_READY,               // 스크립트 생성 완료
        SCRIPT_REVIEW_REQUESTED,    // 1차 검토 요청 (스크립트)
        SCRIPT_APPROVED,            // 1차 승인 (영상 생성 가능)
        PROCESSING,                 // 영상 생성 중
        READY,                      // 영상 생성 완료
        FINAL_REVIEW_REQUESTED,     // 2차 검토 요청 (영상)
        PUBLISHED                   // 최종 승인/게시 (유저 노출)
    }

    @Schema(description = "영상 상태 강제 변경 요청 (어드민 테스트용)")
    public record VideoStatusChangeRequest(
        @Schema(description = "변경할 상태", example = "READY")
        @NotNull(message = "status는 필수입니다")
        VideoStatus status
    ) {}
}
