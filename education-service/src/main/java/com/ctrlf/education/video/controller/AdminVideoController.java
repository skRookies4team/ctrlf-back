package com.ctrlf.education.video.controller;

import com.ctrlf.education.video.dto.VideoDtos.VideoMetaItem;
import com.ctrlf.education.video.dto.VideoDtos.VideoMetaUpdateRequest;
import com.ctrlf.education.video.entity.EducationVideo;
import com.ctrlf.education.video.repository.EducationVideoRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Education-Admin Video", description = "교육 영상 메타 관리 API (ADMIN)")
@RestController
@RequestMapping("/admin/video")
@RequiredArgsConstructor
public class AdminVideoController {

  private final EducationVideoRepository educationVideoRepository;

  @Operation(summary = "영상 상세 조회", description = "영상 메타 정보를 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoMetaItem.class))),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @GetMapping("/{videoId}")
  public ResponseEntity<VideoMetaItem> getVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId) {
    EducationVideo v = educationVideoRepository.findById(videoId)
        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.NOT_FOUND, "영상을 찾을 수 없습니다: " + videoId));
    return ResponseEntity.ok(toMeta(v));
  }

  @Operation(summary = "영상 목록 조회(페이징)", description = "영상 메타 목록을 페이징으로 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoMetaItem.class)))
  })
  @GetMapping("/list")
  public ResponseEntity<List<VideoMetaItem>> listVideos(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    Page<EducationVideo> p = educationVideoRepository.findAll(
        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "orderIndex").and(Sort.by(Sort.Direction.ASC, "createdAt"))));
    return ResponseEntity.ok(p.map(this::toMeta).getContent());
  }

  @Operation(summary = "영상 수정", description = "파일 URL/버전/길이/메인여부/상태/부서코드 등을 부분 업데이트합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공",
      content = @Content(schema = @Schema(implementation = VideoMetaItem.class))),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @PutMapping("/{videoId}")
  public ResponseEntity<VideoMetaItem> updateVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId,
      @Valid @RequestBody VideoMetaUpdateRequest req) {
    EducationVideo v = educationVideoRepository.findById(videoId)
        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.NOT_FOUND, "영상을 찾을 수 없습니다: " + videoId));
    if (req.fileUrl() != null) v.setFileUrl(req.fileUrl());
    if (req.version() != null) v.setVersion(req.version());
    if (req.duration() != null) v.setDuration(req.duration());
    if (req.status() != null) v.setStatus(req.status());
    if (req.targetDeptCode() != null) v.setTargetDeptCode(req.targetDeptCode());
    if (req.orderIndex() != null) v.setOrderIndex(req.orderIndex());
    v = educationVideoRepository.save(v);
    return ResponseEntity.ok(toMeta(v));
  }

  @Operation(summary = "영상 삭제", description = "영상을 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "삭제 성공"),
    @ApiResponse(responseCode = "404", description = "영상을 찾을 수 없음", content = @Content)
  })
  @DeleteMapping("/{videoId}")
  public ResponseEntity<Void> deleteVideo(
      @Parameter(description = "영상 ID", required = true) @PathVariable UUID videoId) {
    if (!educationVideoRepository.existsById(videoId)) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.NOT_FOUND, "영상을 찾을 수 없습니다: " + videoId);
    }
    educationVideoRepository.deleteById(videoId);
    return ResponseEntity.noContent().build();
  }

  private VideoMetaItem toMeta(EducationVideo v) {
    return new VideoMetaItem(
        v.getId(),
        v.getEducationId(),
        v.getGenerationJobId(),
        v.getFileUrl(),
        v.getVersion(),
        v.getDuration(),
        v.getStatus(),
        v.getTargetDeptCode(),
        v.getOrderIndex(),
        v.getCreatedAt() != null ? v.getCreatedAt().toString() : null
    );
  }
}
