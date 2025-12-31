package com.ctrlf.chat.telemetry.controller;

import com.ctrlf.chat.telemetry.dto.TelemetryEventRequestDtos;
import com.ctrlf.chat.telemetry.dto.TelemetryEventResponseDtos;
import com.ctrlf.chat.telemetry.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Telemetry 이벤트 수집 API 컨트롤러
 * 
 * <p>AI에서 전송된 구조화된 이벤트 로그를 수집하는 내부 API입니다.</p>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@Tag(name = "Telemetry-Internal", description = "Telemetry 이벤트 수집 API (Internal)")
@RestController
@RequestMapping("/internal/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping("/events")
    @Operation(
        summary = "이벤트 수집",
        description = "AI에서 생성한 구조화 이벤트를 배치로 수집합니다. Idempotent (중복 eventId는 무시)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수신 완료 (부분 실패 포함)",
            content = @Content(schema = @Schema(implementation = TelemetryEventResponseDtos.EventsResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음"
        ),
        @ApiResponse(
            responseCode = "413",
            description = "Payload 너무 큼"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "레이트 제한 초과"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류"
        )
    })
    public ResponseEntity<TelemetryEventResponseDtos.EventsResponse> collectEvents(
        @RequestBody TelemetryEventRequestDtos.EventsRequest request
    ) {
        return ResponseEntity.ok(telemetryService.collectEvents(request));
    }
}

