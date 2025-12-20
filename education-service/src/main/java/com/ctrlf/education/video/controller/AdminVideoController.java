package com.ctrlf.education.video.controller;

import com.ctrlf.education.video.dto.VideoDtos.VideoCreateRequest;
import com.ctrlf.education.video.dto.VideoDtos.VideoCreateResponse;
import com.ctrlf.education.video.dto.VideoDtos.VideoMetaItem;
import com.ctrlf.education.video.dto.VideoDtos.VideoMetaUpdateRequest;
import com.ctrlf.education.video.dto.VideoDtos.VideoRejectRequest;
import com.ctrlf.education.video.dto.VideoDtos.VideoStatus;
import com.ctrlf.education.video.dto.VideoDtos.VideoStatusResponse;
import com.ctrlf.education.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "Education-Admin Video", description = "교육 영상 메타 관리 API (ADMIN)")
@RestController
@RequestMapping("/admin/videos")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class AdminVideoController {

  private final VideoService videoService;

  // ========================
  // 영상 컨텐츠 생성 플로우 API
  // ========================

  @Operation(summary = "영상 컨텐츠 생성 (프론트 -> 백엔드)", description = "DRAFT 상태의 새 교육 영상 컨텐츠를 생성합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "생성됨",
      content = @Content(schema = @Schema(implementation = VideoCreateResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
  })
  @PostMapping
  public ResponseEntity<VideoCreateResponse> createVideo(
      @Valid @RequestBody VideoCreateRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(videoService.createVideoContent(req));
  }

  @Operation(summary = "검토 요청 (프론트 -> 백엔드)", description = "영상 생성 완료 후 검토자에게 검토를 요청합니다. (READY → REVIEW_REQUESTED)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoStatusResponse.class))),
    @ApiResponse(responseCode = "400", description = "상태 변경 불가", content = @Content),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @PutMapping("/{videoId}/review-request")
  public ResponseEntity<VideoStatusResponse> requestReview(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId) {
    return ResponseEntity.ok(videoService.requestReview(videoId));
  }

  @Operation(summary = "검토 승인 (프론트 -> 백엔드)", description = "검토자가 영상을 승인합니다. (REVIEW_REQUESTED → APPROVED)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoStatusResponse.class))),
    @ApiResponse(responseCode = "400", description = "상태 변경 불가", content = @Content),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @PutMapping("/{videoId}/approve")
  public ResponseEntity<VideoStatusResponse> approveVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId) {
    return ResponseEntity.ok(videoService.approveVideo(videoId));
  }

  @Operation(summary = "검토 반려 (프론트 -> 백엔드)", description = "검토자가 영상을 반려합니다. (REVIEW_REQUESTED → DRAFT)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoStatusResponse.class))),
    @ApiResponse(responseCode = "400", description = "상태 변경 불가", content = @Content),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @PutMapping("/{videoId}/reject")
  public ResponseEntity<VideoStatusResponse> rejectVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId,
      @RequestBody(required = false) VideoRejectRequest req) {
    String reason = req != null ? req.reason() : null;
    return ResponseEntity.ok(videoService.rejectVideo(videoId, reason));
  }

  @Operation(summary = "게시 (프론트 -> 백엔드)", description = "승인된 영상을 유저에게 노출합니다. (APPROVED → ACTIVE)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoStatusResponse.class))),
    @ApiResponse(responseCode = "400", description = "상태 변경 불가", content = @Content),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @PutMapping("/{videoId}/publish")
  public ResponseEntity<VideoStatusResponse> publishVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId) {
    return ResponseEntity.ok(videoService.publishVideo(videoId));
  }

  @Operation(summary = "영상 상태 강제 변경 (* 개발용)", 
      description = "어드민 테스트용: 영상 상태를 강제로 변경합니다. (상태 검증 없음)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoStatusResponse.class))),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @PutMapping("/{videoId}/status")
  public ResponseEntity<VideoStatusResponse> forceChangeStatus(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId,
      @Parameter(description = "변경할 상태", required = true) @RequestParam VideoStatus status) {
    return ResponseEntity.ok(videoService.forceChangeStatus(videoId, status.name()));
  }

  // ========================
  // 영상 메타 CRUD API
  // ========================

  @Operation(summary = "영상 상세 조회 (* 프론트 -> 백엔드)", description = "영상 메타 정보를 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoMetaItem.class))),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @GetMapping("/{videoId}")
  public ResponseEntity<VideoMetaItem> getVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId) {
    return ResponseEntity.ok(videoService.getVideoContent(videoId));
  }

  @Operation(summary = "영상 목록 조회(페이징) (* 프론트 -> 백엔드)", description = "영상 메타 목록을 페이징으로 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoMetaItem.class)))
  })
  @GetMapping("/list")
  public ResponseEntity<List<VideoMetaItem>> listVideos(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    return ResponseEntity.ok(videoService.listVideoContents(page, size));
  }

  @Operation(summary = "영상 수정 (* 프론트 -> 백엔드)", description = "제목/파일 URL/버전/길이/상태/부서코드 등을 부분 업데이트합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoMetaItem.class))),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @PutMapping("/{videoId}")
  public ResponseEntity<VideoMetaItem> updateVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId,
      @Valid @RequestBody VideoMetaUpdateRequest req) {
    return ResponseEntity.ok(videoService.updateVideoContent(videoId, req));
  }

  @Operation(summary = "영상 삭제 (* 프론트 -> 백엔드)", description = "영상을 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "삭제 성공"),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @DeleteMapping("/{videoId}")
  public ResponseEntity<Void> deleteVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId) {
    videoService.deleteVideoContent(videoId);
    return ResponseEntity.noContent().build();
  }
}
