package com.ctrlf.education.script.controller;

import com.ctrlf.education.script.dto.EducationScriptDto.ScriptCompleteCallback;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptCompleteResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptDetailResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptGenerateRequest;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptResponse;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptUpdateRequest;
import com.ctrlf.education.script.dto.EducationScriptDto.ScriptUpdateResponse;
import com.ctrlf.education.script.service.ScriptService;
import com.ctrlf.education.script.service.ScriptService.ScriptGenerationResponse;
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
@RequestMapping("/script")
@RequiredArgsConstructor
public class EducationScriptController {

  private final ScriptService scriptService;

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

  /**
   * 임베딩 완료된 자료를 기반으로 스크립트를 자동 생성합니다.
   * (전처리/임베딩은 infra-service의 POST /rag/documents/upload에서 이미 완료됨)
   *
   * @param documentId 자료 ID (= RagDocument.id)
   * @param request eduId, videoId
   * @return AI 서버 수신/상태 응답
   */
  @Operation(
      summary = "스크립트 자동생성 요청 (프론트 -> 백엔드)",
      description = "임베딩 완료된 자료(documentId)를 기반으로 교육 스크립트를 자동 생성합니다. " +
          "영상 컨텐츠(videoId)에 자료(documentId)가 연결됩니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "요청 전송 성공",
        content = @Content(schema = @Schema(implementation = ScriptGenerationResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
    @ApiResponse(responseCode = "404", description = "영상 컨텐츠를 찾을 수 없음", content = @Content),
    @ApiResponse(responseCode = "500", description = "AI 서버 요청 실패", content = @Content)
  })
  @PostMapping("/generate/{documentId}")
  public ResponseEntity<ScriptGenerationResponse> generateScript(
      @Parameter(description = "자료 ID (= RagDocument.id)", required = true)
      @PathVariable UUID documentId,
      @Valid @RequestBody ScriptGenerateRequest request) {
    return ResponseEntity.ok(
        scriptService.requestScriptGeneration(documentId, request.eduId(), request.videoId()));
  }
}
