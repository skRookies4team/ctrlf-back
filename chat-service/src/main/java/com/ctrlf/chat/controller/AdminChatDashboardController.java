package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.response.ChatDashboardResponse;
import com.ctrlf.chat.dto.response.DashboardResponseDtos;
import com.ctrlf.chat.service.ChatDashboardService;
import com.ctrlf.chat.service.DashboardService;
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
@RequestMapping("/admin/dashboard")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class AdminChatDashboardController {

    private final ChatDashboardService chatDashboardService;
    private final DashboardService dashboardService;

    // ==================== 새 스펙 API ====================

    @GetMapping("/chat/summary")
    @Operation(
        summary = "챗봇 탭 상단 요약 카드 조회",
        description = "새 스펙: period(today|7d|30d|90d), dept(all|dept_id), refresh(true면 캐시 무시)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = DashboardResponseDtos.ChatSummaryResponse.class))
        )
    })
    public ResponseEntity<DashboardResponseDtos.ChatSummaryResponse> getChatSummary(
        @Parameter(description = "기간 (today|7d|30d|90d)", example = "30d")
        @RequestParam(value = "period", defaultValue = "30d") String period,
        @Parameter(description = "부서 필터 (all|dept_id)", example = "all")
        @RequestParam(value = "dept", defaultValue = "all") String dept,
        @Parameter(description = "캐시 무시 여부", example = "false")
        @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        return ResponseEntity.ok(dashboardService.getChatSummary(period, dept, refresh));
    }

    @GetMapping("/chat/trends")
    @Operation(
        summary = "질문 수·에러율 추이 조회",
        description = "새 스펙: period, dept, bucket(day|week), refresh"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = DashboardResponseDtos.ChatTrendsResponse.class))
        )
    })
    public ResponseEntity<DashboardResponseDtos.ChatTrendsResponse> getChatTrends(
        @Parameter(description = "기간 (today|7d|30d|90d)", example = "30d")
        @RequestParam(value = "period", defaultValue = "30d") String period,
        @Parameter(description = "부서 필터 (all|dept_id)", example = "all")
        @RequestParam(value = "dept", defaultValue = "all") String dept,
        @Parameter(description = "버킷 (day|week)", example = "week")
        @RequestParam(value = "bucket", defaultValue = "week") String bucket,
        @Parameter(description = "캐시 무시 여부", example = "false")
        @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        return ResponseEntity.ok(dashboardService.getChatTrends(period, dept, bucket, refresh));
    }

    @GetMapping("/chat/domain-share")
    @Operation(
        summary = "도메인별 질문 비율 조회",
        description = "새 스펙: period, dept, refresh"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = DashboardResponseDtos.DomainShareResponse.class))
        )
    })
    public ResponseEntity<DashboardResponseDtos.DomainShareResponse> getDomainShare(
        @Parameter(description = "기간 (today|7d|30d|90d)", example = "30d")
        @RequestParam(value = "period", defaultValue = "30d") String period,
        @Parameter(description = "부서 필터 (all|dept_id)", example = "all")
        @RequestParam(value = "dept", defaultValue = "all") String dept,
        @Parameter(description = "캐시 무시 여부", example = "false")
        @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        return ResponseEntity.ok(dashboardService.getDomainShare(period, dept, refresh));
    }

    @GetMapping("/metrics/security")
    @Operation(
        summary = "지표 탭 — 보안·PII",
        description = "새 스펙: period, dept, refresh"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = DashboardResponseDtos.SecurityMetricsResponse.class))
        )
    })
    public ResponseEntity<DashboardResponseDtos.SecurityMetricsResponse> getSecurityMetrics(
        @Parameter(description = "기간 (today|7d|30d|90d)", example = "30d")
        @RequestParam(value = "period", defaultValue = "30d") String period,
        @Parameter(description = "부서 필터 (all|dept_id)", example = "all")
        @RequestParam(value = "dept", defaultValue = "all") String dept,
        @Parameter(description = "캐시 무시 여부", example = "false")
        @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        return ResponseEntity.ok(dashboardService.getSecurityMetrics(period, dept, refresh));
    }

    @GetMapping("/metrics/performance")
    @Operation(
        summary = "지표 탭 — 성능·장애",
        description = "새 스펙: period, dept, refresh"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = DashboardResponseDtos.PerformanceMetricsResponse.class))
        )
    })
    public ResponseEntity<DashboardResponseDtos.PerformanceMetricsResponse> getPerformanceMetrics(
        @Parameter(description = "기간 (today|7d|30d|90d)", example = "30d")
        @RequestParam(value = "period", defaultValue = "30d") String period,
        @Parameter(description = "부서 필터 (all|dept_id)", example = "all")
        @RequestParam(value = "dept", defaultValue = "all") String dept,
        @Parameter(description = "캐시 무시 여부", example = "false")
        @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
    ) {
        return ResponseEntity.ok(dashboardService.getPerformanceMetrics(period, dept, refresh));
    }

    @GetMapping("/route-ratio")
    @Operation(
        summary = "라우트별 질문 비율 조회",
        description = "RAG, LLM, Incident, FAQ, 기타 라우트별 질문 비율을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ChatDashboardResponse.RouteRatioResponse.class))
        )
    })
    public ResponseEntity<ChatDashboardResponse.RouteRatioResponse> getRouteRatio(
        @Parameter(description = "기간 (일수, 7/30/90)", example = "30")
        @RequestParam(value = "period", required = false) Integer period,
        @Parameter(description = "부서 필터", example = "총무팀")
        @RequestParam(value = "department", required = false) String department
    ) {
        return ResponseEntity.ok(chatDashboardService.getRouteRatio(period, department));
    }

    @GetMapping("/top-keywords")
    @Operation(
        summary = "최근 많이 질문된 키워드 Top 5 조회",
        description = "최근 많이 질문된 키워드와 질문 횟수를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ChatDashboardResponse.TopKeywordsResponse.class))
        )
    })
    public ResponseEntity<ChatDashboardResponse.TopKeywordsResponse> getTopKeywords(
        @Parameter(description = "기간 (일수, 7/30/90)", example = "30")
        @RequestParam(value = "period", required = false) Integer period,
        @Parameter(description = "부서 필터", example = "총무팀")
        @RequestParam(value = "department", required = false) String department
    ) {
        return ResponseEntity.ok(chatDashboardService.getTopKeywords(period, department));
    }

    @GetMapping("/question-trend")
    @Operation(
        summary = "질문 수 · 에러율 추이 조회",
        description = "주간별 질문 수와 에러율 추이를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ChatDashboardResponse.QuestionTrendResponse.class))
        )
    })
    public ResponseEntity<ChatDashboardResponse.QuestionTrendResponse> getQuestionTrend(
        @Parameter(description = "기간 (일수, 7/30/90)", example = "30")
        @RequestParam(value = "period", required = false) Integer period,
        @Parameter(description = "부서 필터", example = "총무팀")
        @RequestParam(value = "department", required = false) String department
    ) {
        return ResponseEntity.ok(chatDashboardService.getQuestionTrend(period, department));
    }

    @GetMapping("/domain-ratio")
    @Operation(
        summary = "도메인별 질문 비율 조회",
        description = "규정, FAQ, 교육, 퀴즈, 기타 도메인별 질문 비율을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(schema = @Schema(implementation = ChatDashboardResponse.DomainRatioResponse.class))
        )
    })
    public ResponseEntity<ChatDashboardResponse.DomainRatioResponse> getDomainRatio(
        @Parameter(description = "기간 (일수, 7/30/90)", example = "30")
        @RequestParam(value = "period", required = false) Integer period,
        @Parameter(description = "부서 필터", example = "총무팀")
        @RequestParam(value = "department", required = false) String department
    ) {
        return ResponseEntity.ok(chatDashboardService.getDomainRatio(period, department));
    }
}

