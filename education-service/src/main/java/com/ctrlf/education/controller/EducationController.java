package com.ctrlf.education.controller;

import com.ctrlf.education.dto.EducationRequests.CreateEducationRequest;
import com.ctrlf.education.dto.EducationRequests.UpdateEducationRequest;
import com.ctrlf.education.dto.EducationRequests.VideoProgressUpdateRequest;
import com.ctrlf.education.dto.EducationResponses.CreateEducationResponse;
import com.ctrlf.education.dto.EducationResponses.EducationDetailResponse;
import com.ctrlf.education.dto.EducationResponses.EducationVideosResponse;
import com.ctrlf.education.dto.EducationResponses.MandatoryEducationDto;
import com.ctrlf.education.dto.EducationResponses.VideoProgressResponse;
import com.ctrlf.education.dto.EducationResponses.UpdateEducationResponse;

import com.ctrlf.education.service.EducationService;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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

import com.ctrlf.common.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 * 교육 도메인 REST 컨트롤러.
 * <p>
 * - 목록/상세/수정/삭제 등 교육 리소스 CRUD와
 * <p>
 * - 영상 목록 및 진행률(수강) 업데이트, 교육 이수 처리 API를 제공합니다.
 * <p>
 * JWT 값을 사용자 UUID로 해석하여 사용자별 진행 정보를 처리합니다.
 */
@RestController
@Tag(name = "Education", description = "교육 리소스 API")
@SecurityRequirement(name = "bearer-jwt")
@RequestMapping
public class EducationController {

    private final EducationService educationService;

    public EducationController(EducationService educationService) {
        this.educationService = educationService;
    }

    /**
     * 교육 생성.
     *
     * @param req 생성 요청 본문
     * @return 생성된 교육 ID와 상태
     */
    @PostMapping("/edu")
    @Operation(summary = "교육 생성", description = "교육을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성됨"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<CreateEducationResponse> createEducation(@jakarta.validation.Valid @RequestBody CreateEducationRequest req) {
        UUID id = educationService.createEducation(req);
        return ResponseEntity
            .created(URI.create("/edu/" + id))
            .body(new CreateEducationResponse(id));
    }

    /**
     * 교육 목록 조회.
     *
     * @param page 페이지 번호(0-base)
     * @param size 페이지 크기(기본 10)
     * @param completed 이수 여부 필터(로그인 사용자 있을 때만 유효)
     * @param year 연도 필터
     * @param category 카테고리 필터(MANDATORY/JOB/ETC)
     * @param jwt 인증 토큰
     * @return 간략 교육 목록
     */
    @GetMapping("/edus")
    @Operation(summary = "교육 목록 조회", description = "페이지네이션 및 필터로 교육 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity<List<MandatoryEducationDto>> getEducations(
        @Parameter(description = "페이지 번호(0-base)") @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
        @Parameter(description = "페이지 크기(기본 10)") @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
        @Parameter(description = "이수 여부 필터") @RequestParam(name = "completed", required = false) Boolean completed,
        @Parameter(description = "연도 필터") @RequestParam(name = "year", required = false) Integer year,
        @Parameter(description = "카테고리 필터(MANDATORY/JOB/ETC)") @RequestParam(name = "category", required = false) String category,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Optional<UUID> userUuid = SecurityUtils.extractUserUuid(jwt);
        
        List<MandatoryEducationDto> result = educationService.getEducations(
            page, size, Optional.ofNullable(completed), Optional.ofNullable(year), Optional.ofNullable(category), userUuid
        );
        return ResponseEntity.ok(result);
    }

    /**
     * 교육 상세 조회.
     *
     * @param id 교육 ID
     * @return 상세 정보
     */
    @GetMapping("/edu/{id}")
    @Operation(summary = "교육 상세 조회", description = "교육 기본 정보와 차시 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "404", description = "교육을 찾을 수 없음")
    })
    public ResponseEntity<EducationDetailResponse> getEducationDetail(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(educationService.getEducationDetail(id));
    }

    /**
     * 교육 수정.
     *
     * @param id 교육 ID
     * @param req 수정 요청 본문(부분 수정 허용)
     * @return 업데이트 결과
     */
    @PutMapping("/edu/{id}")
    @Operation(summary = "교육 수정", description = "교육 정보를 부분 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "교육을 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> updateEducation(
        @Parameter(description = "교육 ID") @PathVariable("id") UUID id,
        @RequestBody UpdateEducationRequest req
    ) {
        Instant updatedAt = educationService.updateEducation(id, req);
        return ResponseEntity.ok(Map.of("eduId", id, "updated", true, "updatedAt", updatedAt.toString()));
    }

    /**
     * 교육 삭제.
     *
     * @param id 교육 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/edu/{id}")
    @Operation(summary = "교육 삭제", description = "교육을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "404", description = "교육을 찾을 수 없음")
    })
    public ResponseEntity<Map<String, Object>> deleteEducation(@PathVariable("id") UUID id) {
        educationService.deleteEducation(id);
        return ResponseEntity.ok(Map.of("eduId", id, "status", "DELETED"));
    }

    /**
     * 교육 영상 목록 및 사용자별 진행 정보 조회.
     *
     * @param id 교육 ID
     * @param jwt 인증 토큰
     * @return 영상 목록과 진행 정보
     */
    @GetMapping("/edu/{id}/videos")
    @Operation(summary = "교육 영상 목록 조회", description = "교육에 포함된 영상 목록과 사용자별 진행 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "404", description = "교육을 찾을 수 없음")
    })
    public ResponseEntity<EducationVideosResponse> getEducationVideos(
        @Parameter(description = "교육 ID") @PathVariable("id") UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Optional<UUID> userUuid = SecurityUtils.extractUserUuid(jwt);
        return ResponseEntity.ok(educationService.getEducationVideos(id, userUuid));
    }

    /**
     * 영상 시청 진행률 업데이트.
     *
     * @param educationId 교육 ID
     * @param videoId 영상 ID
     * @param jwt 인증 토큰(사용자 UUID 파싱)
     * @param req 진행률/시청시간 정보
     * @return 업데이트 결과 요약
     */
    @PostMapping("/edu/{educationId}/video/{videoId}/progress")
    @Operation(summary = "영상 시청 진행률 업데이트", description = "특정 교육의 특정 영상에 대한 사용자 시청 진행 정보를 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음")
    })
    public ResponseEntity<VideoProgressResponse> updateVideoProgress(
        @Parameter(description = "교육 ID") @PathVariable UUID educationId,
        @Parameter(description = "영상 ID") @PathVariable UUID videoId,
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody VideoProgressUpdateRequest req
    ) {
        UUID userUuid = SecurityUtils.extractUserUuid(jwt).orElse(null);
        return ResponseEntity.ok(educationService.updateVideoProgress(educationId, videoId, userUuid, req));
    }

    /**
     * 교육 이수 처리.
     *
     * @param id 교육 ID
     * @param jwt 인증 토큰(사용자 UUID 파싱)
     * @return 이수 처리 결과
     */
    @PostMapping("/edu/{id}/complete")
    @Operation(summary = "교육 이수 처리", description = "모든 영상 이수 여부를 확인하여 교육 이수 처리를 수행합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이수 완료"),
        @ApiResponse(responseCode = "400", description = "이수 조건 미충족 또는 잘못된 요청")
    })
    public ResponseEntity<Map<String, Object>> completeEducation(
        @Parameter(description = "교육 ID") @PathVariable("id") UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userUuid = SecurityUtils.extractUserUuid(jwt).orElse(null);
        Map<String, Object> body = educationService.completeEducation(id, userUuid);
        boolean ok = "COMPLETED".equals(body.get("status"));
        return ResponseEntity.status(ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(body);
    }
}

