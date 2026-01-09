package com.ctrlf.education.script.controller;

import com.ctrlf.common.security.SecurityUtils;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptCompleteCallback;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptCompleteResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptDetailResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptLookupResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptUpdateRequest;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptUpdateResponse;
import com.ctrlf.education.script.service.ScriptService;
import com.ctrlf.education.video.dto.VideoDtos.VideoRejectRequest;
import com.ctrlf.education.video.dto.VideoDtos.VideoStatusResponse;
import com.ctrlf.education.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Script", description = "스크립트 관련 API")
@RestController
@RequestMapping("/scripts")
@RequiredArgsConstructor
public class EducationScriptController {

  private final ScriptService scriptService;
  private final VideoService videoService;

  @Operation(
      summary = "스크립트 ID 조회 (프론트 -> 백엔드)",
      description = "videoId 또는 educationId로 스크립트 ID를 조회합니다. 둘 다 제공된 경우 videoId 우선."
  )
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ScriptLookupResponse.class))),
    @ApiResponse(responseCode = "400", description = "videoId와 educationId 모두 없음", content = @Content),
    @ApiResponse(responseCode = "404", description = "스크립트를 찾을 수 없음", content = @Content)
  })
  @GetMapping("/lookup")
  public ResponseEntity<ScriptLookupResponse> lookupScriptId(
      @Parameter(description = "영상 ID") @RequestParam(required = false) UUID videoId,
      @Parameter(description = "교육 ID") @RequestParam(required = false) UUID educationId) {
    return ResponseEntity.ok(scriptService.findScriptId(videoId, educationId));
  }

  @Operation(summary = "스크립트 조회 (* 개발용)", description = "AI가 생성한 스크립트를 조회합니다. (챕터/씬 포함)")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ScriptDetailResponse.class))),
    @ApiResponse(responseCode = "404", description = "스크립트를 찾을 수 없음", content = @Content)
  })
  @GetMapping("/{scriptId}")
  public ResponseEntity<ScriptDetailResponse> getScript(
      @Parameter(description = "스크립트 ID", required = true) @PathVariable UUID scriptId) {
    return ResponseEntity.ok(scriptService.getScript(scriptId));
  }

  @Operation(summary = "스크립트 목록 조회 (프론트 -> 백엔드)", description = "스크립트 목록을 페이징으로 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ScriptResponse.class)))
  })
  @GetMapping
  public ResponseEntity<List<ScriptResponse>> listScripts(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    return ResponseEntity.ok(scriptService.listScripts(page, size));
  }

  @Operation(summary = "스크립트 삭제 (프론트 -> 백엔드)", description = "스크립트를 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "삭제 성공"),
    @ApiResponse(responseCode = "404", description = "스크립트를 찾을 수 없음", content = @Content)
  })
  @DeleteMapping("/{scriptId}")
  public ResponseEntity<Void> deleteScript(
      @Parameter(description = "스크립트 ID", required = true) @PathVariable UUID scriptId) {
    scriptService.deleteScript(scriptId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "스크립트 수정 (프론트 -> 백엔드)",
      description = "관리자가 스크립트(rawPayload) 및 챕터/씬을 수정합니다. (챕터/씬은 전체 교체)")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "수정 성공",
        content = @Content(schema = @Schema(implementation = ScriptUpdateResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
    @ApiResponse(responseCode = "404", description = "스크립트를 찾을 수 없음", content = @Content)
  })
  @PutMapping("/{scriptId}")
  public ResponseEntity<ScriptUpdateResponse> updateScript(
      @Parameter(description = "스크립트 ID", required = true) @PathVariable UUID scriptId,
      @Valid @RequestBody ScriptUpdateRequest request) {
    return ResponseEntity.ok(scriptService.updateScript(scriptId, request));
  }

  @Operation(
      summary = "스크립트 생성 완료 콜백 (AI -> 백엔드)",
      description = "AI 서버가 전처리 & 스크립트 생성 완료 후 백엔드로 결과를 전달합니다. (내부 API)",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ScriptCompleteCallback.class),
              examples = @ExampleObject(
                  name = "스크립트 JSON 예시",
                  value = """
                      {
                        "videoId": "550e8400-e29b-41d4-a716-446655440003",
                        "script": {
                          "title": "직장내괴롭힘 교육 영상",
                          "total_duration_sec": 720,
                          "chapters": [
                            {
                              "title": "괴롭힘",
                              "duration_sec": 180,
                              "scenes": [
                                {
                                  "scene_id": 1,
                                  "purpose": "hook",
                                  "visual": "자료 원문 문장(텍스트) 강조",
                                  "narration": "직장 내 괴롭힘이란...",
                                  "caption": "직장 내 괴롭힘이란...",
                                  "duration_sec": 15,
                                  "source_chunks": [1, 2, 3]
                                }
                              ]
                            }
                          ]
                        },
                        "version": 1
                      }
                      """
              )
          )
      )
  )
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "콜백 처리 성공",
        content = @Content(schema = @Schema(implementation = ScriptCompleteResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
  })
  @PostMapping("/complete")
  public ResponseEntity<ScriptCompleteResponse> handleScriptComplete(
      @Valid @RequestBody ScriptCompleteCallback callback) {
    return ResponseEntity.ok(scriptService.handleScriptComplete(callback));
  }

  @Operation(
      summary = "스크립트 1차 승인 (프론트 -> 백엔드)",
      description = "스크립트를 승인합니다. SCRIPT_REVIEW_REQUESTED → SCRIPT_APPROVED 상태로 변경됩니다."
  )
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "승인 성공",
        content = @Content(schema = @Schema(implementation = VideoStatusResponse.class))),
    @ApiResponse(responseCode = "400", description = "상태 변경 불가", content = @Content),
    @ApiResponse(responseCode = "404", description = "스크립트/영상을 찾을 수 없음", content = @Content)
  })
  @PostMapping("/{scriptId}/approve")
  public ResponseEntity<VideoStatusResponse> approveScript(
      @Parameter(description = "스크립트 ID", required = true) @PathVariable UUID scriptId) {
    return ResponseEntity.ok(videoService.approveScript(scriptId));
  }

  @Operation(
      summary = "스크립트 1차 반려 (프론트 -> 백엔드)",
      description = "스크립트를 반려합니다. SCRIPT_REVIEW_REQUESTED → SCRIPT_READY 상태로 변경됩니다."
  )
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "반려 성공",
        content = @Content(schema = @Schema(implementation = VideoStatusResponse.class))),
    @ApiResponse(responseCode = "400", description = "상태 변경 불가", content = @Content),
    @ApiResponse(responseCode = "404", description = "스크립트/영상을 찾을 수 없음", content = @Content)
  })
  @PostMapping("/{scriptId}/reject")
  public ResponseEntity<VideoStatusResponse> rejectScript(
      @Parameter(description = "스크립트 ID", required = true) @PathVariable UUID scriptId,
      @RequestBody(required = false) VideoRejectRequest req,
      @AuthenticationPrincipal Jwt jwt) {
    String reason = req != null ? req.reason() : null;
    UUID reviewerUuid = SecurityUtils.extractUserUuid(jwt)
        .orElseThrow(() -> new IllegalArgumentException("사용자 UUID를 추출할 수 없습니다."));
    return ResponseEntity.ok(videoService.rejectScript(scriptId, reason, reviewerUuid));
  }
}
