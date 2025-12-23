package com.ctrlf.education.video.controller;

import com.ctrlf.education.video.dto.VideoDtos.InternalSourceSetDocumentsResponse;
import com.ctrlf.education.video.dto.VideoDtos.SourceSetCompleteCallback;
import com.ctrlf.education.video.dto.VideoDtos.SourceSetCompleteResponse;
import com.ctrlf.education.video.service.SourceSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 소스셋 내부 API 컨트롤러 (FastAPI ↔ Spring).
 * 내부 서비스 간 통신용으로 X-Internal-Token 인증을 사용합니다.
 */
@Tag(name = "Internal - SourceSet", description = "소스셋 내부 API (FastAPI ↔ Spring)")
@RestController
@RequestMapping("/internal")
@SecurityRequirement(name = "internal-token")
@RequiredArgsConstructor
public class InternalSourceSetController {

    private final SourceSetService sourceSetService;

    /**
     * 소스셋 문서 목록 조회 (FastAPI → Spring).
     * FastAPI가 sourceSet에 포함된 RagDocument 목록을 조회합니다.
     */
    @GetMapping("/source-sets/{sourceSetId}/documents")
    @Operation(
        summary = "소스셋 문서 목록 조회 (내부 API)",
        description = "FastAPI가 sourceSet에 포함된 RagDocument 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = InternalSourceSetDocumentsResponse.class))),
        @ApiResponse(responseCode = "401", description = "내부 토큰 오류"),
        @ApiResponse(responseCode = "404", description = "소스셋을 찾을 수 없음")
    })
    public ResponseEntity<InternalSourceSetDocumentsResponse> getSourceSetDocuments(
        @Parameter(description = "소스셋 ID", required = true)
        @PathVariable UUID sourceSetId
    ) {
        return ResponseEntity.ok(sourceSetService.getSourceSetDocuments(sourceSetId));
    }

    /**
     * 소스셋 완료 콜백 (FastAPI → Spring).
     * sourceSet 오케스트레이션 완료 결과를 Spring에 전달합니다.
     */
    @PostMapping("/callbacks/source-sets/{sourceSetId}/complete")
    @Operation(
        summary = "소스셋 완료 콜백 (내부 API)",
        description = "sourceSet 오케스트레이션 완료 결과를 Spring에 전달합니다 (성공/실패)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "콜백 처리 성공",
            content = @Content(schema = @Schema(implementation = SourceSetCompleteResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "내부 토큰 오류"),
        @ApiResponse(responseCode = "404", description = "소스셋을 찾을 수 없음")
    })
    public ResponseEntity<SourceSetCompleteResponse> handleSourceSetComplete(
        @Parameter(description = "소스셋 ID", required = true)
        @PathVariable UUID sourceSetId,
        @Valid @RequestBody SourceSetCompleteCallback callback
    ) {
        return ResponseEntity.ok(sourceSetService.handleSourceSetComplete(sourceSetId, callback));
    }
}
