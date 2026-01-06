package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.response.ChatDashboardResponse;
import com.ctrlf.chat.service.ChatDashboardService;
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
 * 챗봇 관리자 대시보드 통계 API 컨트롤러
 * 
 * <p>관리자가 챗봇 사용 현황을 모니터링하고 통계를 조회하는 API를 제공합니다.</p>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@Tag(name = "Chat-Admin", description = "챗봇 관리자 대시보드 통계 API (ADMIN)")
@RestController
@RequestMapping("/admin/dashboard/chat")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class AdminChatDashboardController {

    private final ChatDashboardService chatDashboardService;

    @GetMapping("/summary")
    @Operation(
        summary = "대시보드 요약 통계 조회",
        description = "챗봇 탭 상단 요약 카드 데이터를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ChatDashboardResponse.DashboardSummaryResponse.class))
        )
    })
    public ResponseEntity<ChatDashboardResponse.DashboardSummaryResponse> getDashboardSummary(
        @Parameter(description = "기간 (today | 7d | 30d | 90d)", example = "30d")
        @RequestParam(value = "period", required = false, defaultValue = "30d") String period,
        @Parameter(description = "부서 필터 (all 또는 dept_id)", example = "all")
        @RequestParam(value = "dept", required = false, defaultValue = "all") String dept,
        @Parameter(description = "캐시 무시 여부", example = "false")
        @RequestParam(value = "refresh", required = false, defaultValue = "false") Boolean refresh
    ) {
        return ResponseEntity.ok(chatDashboardService.getDashboardSummary(period, dept, refresh));
    }

    @GetMapping("/trends")
    @Operation(
        summary = "질문 수 · 에러율 추이 조회",
        description = "질문 수와 에러율 추이를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ChatDashboardResponse.TrendsResponse.class))
        )
    })
    public ResponseEntity<ChatDashboardResponse.TrendsResponse> getTrends(
        @Parameter(description = "기간 (today | 7d | 30d | 90d)", example = "30d")
        @RequestParam(value = "period", required = false, defaultValue = "30d") String period,
        @Parameter(description = "부서 필터 (all 또는 dept_id)", example = "all")
        @RequestParam(value = "dept", required = false, defaultValue = "all") String dept,
        @Parameter(description = "버킷 타입 (day | week)", example = "week")
        @RequestParam(value = "bucket", required = false, defaultValue = "week") String bucket,
        @Parameter(description = "캐시 무시 여부", example = "false")
        @RequestParam(value = "refresh", required = false, defaultValue = "false") Boolean refresh
    ) {
        return ResponseEntity.ok(chatDashboardService.getTrends(period, dept, bucket, refresh));
    }

    @GetMapping("/domain-share")
    @Operation(
        summary = "도메인별 질문 비율 조회",
        description = "도메인별 질문 비율(규정/FAQ/교육/퀴즈/기타)을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ChatDashboardResponse.DomainShareResponse.class))
        )
    })
    public ResponseEntity<ChatDashboardResponse.DomainShareResponse> getDomainShare(
        @Parameter(description = "기간 (today | 7d | 30d | 90d)", example = "30d")
        @RequestParam(value = "period", required = false, defaultValue = "30d") String period,
        @Parameter(description = "부서 필터 (all 또는 dept_id)", example = "all")
        @RequestParam(value = "dept", required = false, defaultValue = "all") String dept,
        @Parameter(description = "캐시 무시 여부", example = "false")
        @RequestParam(value = "refresh", required = false, defaultValue = "false") Boolean refresh
    ) {
        return ResponseEntity.ok(chatDashboardService.getDomainShare(period, dept, refresh));
    }
}

