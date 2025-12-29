package com.ctrlf.education.script.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 스크립트 관련 API의 요청/응답 DTO 모음.
 */
public final class EducationScriptDto {

  private EducationScriptDto() {}

  // ========================
  // 스크립트 조회/상세
  // ========================

  @Schema(description = "스크립트 조회 응답")
  public record ScriptResponse(
      @Schema(description = "스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440000")
      UUID scriptId,
      @Schema(description = "교육 ID", example = "550e8400-e29b-41d4-a716-446655440001")
      UUID educationId,
      @Schema(description = "스크립트 내용", example = "교육 영상 스크립트 내용...")
      String script,
      @Schema(description = "스크립트 버전", example = "1")
      Integer version) {}

  @Schema(description = "스크립트 상세 조회 응답 (챕터/씬 포함)")
  public record ScriptDetailResponse(
      @Schema(description = "스크립트 ID") UUID scriptId,
      @Schema(description = "교육 ID") UUID educationId,
      @Schema(description = "영상 ID") UUID videoId,
      @Schema(description = "제목") String title,
      @Schema(description = "총 길이(초)") Integer totalDurationSec,
      @Schema(description = "스크립트 버전") Integer version,
      @Schema(description = "사용 LLM") String llmModel,
      @Schema(description = "원본 JSON") String rawPayload,
      @Schema(description = "챕터 목록") java.util.List<ChapterItem> chapters) {}

  @Schema(description = "챕터 정보")
  public record ChapterItem(
      @Schema(description = "챕터 ID") UUID chapterId,
      @Schema(description = "순서(0-base)") Integer index,
      @Schema(description = "제목") String title,
      @Schema(description = "길이(초)") Integer durationSec,
      @Schema(description = "씬 목록") java.util.List<SceneItem> scenes) {}

  @Schema(description = "씬 정보")
  public record SceneItem(
      @Schema(description = "씬 ID") UUID sceneId,
      @Schema(description = "챕터 내 순서") Integer index,
      @Schema(description = "목적(hook/concept/example/summary)") String purpose,
      @Schema(description = "내레이션") String narration,
      @Schema(description = "자막") String caption,
      @Schema(description = "시각 연출") String visual,
      @Schema(description = "길이(초)") Integer durationSec,
      @Schema(description = "근거 청크 인덱스") int[] sourceChunkIndexes,
      @Schema(description = "신뢰도") Float confidenceScore) {}

  // ========================
  // 스크립트 수정
  // ========================

  @Schema(description = "스크립트 수정 요청 (rawPayload 또는 챕터/씬 전체 교체)")
  public record ScriptUpdateRequest(
      @Schema(description = "수정할 스크립트 원본 JSON(rawPayload). 제공 시 rawPayload를 업데이트합니다.")
      String script,
      @Schema(description = "챕터/씬 목록. 제공 시 기존 챕터/씬을 삭제하고 전부 교체합니다.")
      java.util.List<ChapterUpsert> chapters) {}

  @Schema(description = "챕터 업서트")
  public record ChapterUpsert(
      @Schema(description = "순서(0-base)", example = "0") Integer index,
      @Schema(description = "챕터 제목", example = "괴롭힘") @NotBlank String title,
      @Schema(description = "챕터 길이(초)", example = "180") Integer durationSec,
      @Schema(description = "씬 목록") java.util.List<SceneUpsert> scenes) {}

  @Schema(description = "씬 업서트")
  public record SceneUpsert(
      @Schema(description = "챕터 내 순서", example = "1") Integer index,
      @Schema(description = "목적", example = "hook") String purpose,
      @Schema(description = "내레이션") String narration,
      @Schema(description = "자막") String caption,
      @Schema(description = "시각 연출") String visual,
      @Schema(description = "길이(초)", example = "15") Integer durationSec,
      @Schema(description = "근거 청크 인덱스") int[] sourceChunkIndexes,
      @Schema(description = "신뢰도", example = "0.9") Float confidenceScore) {}

  @Schema(description = "스크립트 수정 응답")
  public record ScriptUpdateResponse(
      @Schema(description = "수정 성공 여부", example = "true") boolean updated,
      @Schema(description = "스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440000")
      UUID scriptId) {}

  // ========================
  // 스크립트 생성 완료 콜백
  // ========================

  @Schema(description = "스크립트 생성 완료 콜백 요청 (AI 서버 → 백엔드)")
  public record ScriptCompleteCallback(
      @Schema(description = "영상 컨텐츠 ID", example = "550e8400-e29b-41d4-a716-446655440003")
      @NotNull(message = "videoId는 필수입니다")
      UUID videoId,
      @Schema(description = "LLM이 자동 생성한 스크립트 (JSON 객체)")
      @NotNull(message = "script는 필수입니다")
      Object script,
      @Schema(description = "스크립트 버전 번호", example = "1")
      @NotNull(message = "version은 필수입니다")
      Integer version) {}

  @Schema(description = "스크립트 생성 완료 콜백 응답")
  public record ScriptCompleteResponse(
      @Schema(description = "저장 성공 여부", example = "true") boolean saved,
      @Schema(description = "스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440001")
      UUID scriptId) {}

  // ========================
  // 스크립트 ID 조회
  // ========================

  @Schema(description = "스크립트 ID 조회 응답 (videoId 또는 educationId로 조회)")
  public record ScriptLookupResponse(
      @Schema(description = "스크립트 ID", example = "550e8400-e29b-41d4-a716-446655440000")
      UUID scriptId,
      @Schema(description = "교육 ID", example = "550e8400-e29b-41d4-a716-446655440001")
      UUID educationId,
      @Schema(description = "영상 ID", example = "550e8400-e29b-41d4-a716-446655440002")
      UUID videoId,
      @Schema(description = "스크립트 제목")
      String title,
      @Schema(description = "스크립트 버전", example = "1")
      Integer version,
      @Schema(description = "스크립트 상태 (DRAFT, REVIEW_REQUESTED, APPROVED, REJECTED)")
      String status) {}

  // ========================
  // 렌더 스펙 (AI 서버용 내부 API)
  // ========================

  @Schema(description = "렌더 스펙 응답 (AI 서버 → 백엔드 내부 API)")
  public record RenderSpecResponse(
      @JsonProperty("script_id") @Schema(description = "스크립트 ID") String scriptId,
      @JsonProperty("video_id") @Schema(description = "영상 ID") String videoId,
      @JsonProperty("title") @Schema(description = "영상 제목") String title,
      @JsonProperty("total_duration_sec") @Schema(description = "총 영상 길이 (초)") Double totalDurationSec,
      @JsonProperty("scenes") @Schema(description = "씬 목록") java.util.List<RenderSceneItem> scenes) {}

  @Schema(description = "렌더 씬 정보")
  public record RenderSceneItem(
      @JsonProperty("scene_id") @Schema(description = "씬 ID") String sceneId,
      @JsonProperty("scene_order") @Schema(description = "씬 순서 (1부터 시작)") Integer sceneOrder,
      @JsonProperty("chapter_title") @Schema(description = "챕터 제목") String chapterTitle,
      @JsonProperty("purpose") @Schema(description = "씬 목적 (hook, explanation, example, summary 등)") String purpose,
      @JsonProperty("narration") @Schema(description = "나레이션 텍스트") String narration,
      @JsonProperty("caption") @Schema(description = "화면 캡션") String caption,
      @JsonProperty("duration_sec") @Schema(description = "씬 지속 시간 (초)") Double durationSec,
      @JsonProperty("visual_spec") @Schema(description = "시각 사양") RenderVisualSpec visualSpec) {}

  @Schema(description = "시각 사양")
  public record RenderVisualSpec(
      @JsonProperty("type") @Schema(description = "시각 타입", example = "TEXT_HIGHLIGHT") String type,
      @JsonProperty("text") @Schema(description = "표시 텍스트") String text,
      @JsonProperty("highlight_terms") @Schema(description = "강조 용어 목록") java.util.List<String> highlightTerms) {}

}
