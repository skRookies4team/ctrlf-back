package com.ctrlf.infra.telemetry.controller;

import com.ctrlf.infra.telemetry.dto.TelemetryDtos;
import com.ctrlf.infra.telemetry.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 로그 조회 API 컨트롤러
 * 
 * <p>FAQ 자동 생성 등을 위한 AI 로그 조회 API를 제공합니다.</p>
 */
@Tag(name = "AI-Logs", description = "AI 로그 조회 API")
@RestController
@RequestMapping("/api/ai-logs")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class AiLogController {

    private final TelemetryService telemetryService;

    /**
     * AI 로그 조회
     * 
     * <p>CHAT_TURN 이벤트에서 domain, intent, route, question_masked 필드를 추출하여 반환합니다.</p>
     */
    @GetMapping
    @Operation(
        summary = "AI 로그 조회",
        description = "FAQ 자동 생성을 위한 AI 로그를 조회합니다. CHAT_TURN 이벤트에서 domain, intent, route, question_masked 필드를 추출합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = TelemetryDtos.AiLogResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<TelemetryDtos.AiLogResponse> getAiLogs(
        @Parameter(description = "조회할 로그 개수 (기본값: 500, 최대: 1000)", example = "500")
        @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(telemetryService.getAiLogs(limit));
    }
}

