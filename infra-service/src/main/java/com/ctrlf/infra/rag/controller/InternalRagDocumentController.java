package com.ctrlf.infra.rag.controller;

import static com.ctrlf.infra.rag.dto.RagDtos.*;

import com.ctrlf.infra.rag.service.RagDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/rag/documents")
@RequiredArgsConstructor
@Tag(name = "Internal RAG - Documents", description = "RAG 문서 내부 API (AI 서버 → Backend)")
/**
 * RAG 문서 내부 API 컨트롤러.
 * AI 서버에서 호출하는 내부 API만 포함합니다.
 */
public class InternalRagDocumentController {

    private final RagDocumentService ragDocumentService;

    @PatchMapping("/{ragDocumentPk}/status")
    @Operation(
        summary = "사규 상태 업데이트 (AI -> Backend 내부 API)",
        description = "AI 서버가 사규 문서의 임베딩 처리 상태를 업데이트합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 업데이트 성공",
            content = @Content(schema = @Schema(implementation = InternalUpdateStatusResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "내부 토큰 오류"),
        @ApiResponse(responseCode = "404", description = "문서를 찾을 수 없음")
    })
    public ResponseEntity<InternalUpdateStatusResponse> updateDocumentStatus(
        @Parameter(description = "RAG 문서 ID (UUID)", required = true) 
        @PathVariable("ragDocumentPk") UUID ragDocumentPk,
        @RequestHeader(value = "X-Internal-Token", required = false) String internalToken,
        @Valid @RequestBody InternalUpdateStatusRequest req
    ) {
        return ResponseEntity.ok(ragDocumentService.updateDocumentStatus(ragDocumentPk, req));
    }
}

