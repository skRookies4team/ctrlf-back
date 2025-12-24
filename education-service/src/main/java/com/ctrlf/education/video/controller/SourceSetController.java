package com.ctrlf.education.video.controller;

import com.ctrlf.common.security.SecurityUtils;
import com.ctrlf.education.video.dto.VideoDtos.SourceSetCreateRequest;
import com.ctrlf.education.video.dto.VideoDtos.SourceSetCreateResponse;
import com.ctrlf.education.video.dto.VideoDtos.SourceSetUpdateRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 소스셋(SourceSet) REST 컨트롤러.
 * 여러 문서를 묶어 스크립트/영상 제작 단위를 관리합니다.
 */
@Tag(name = "Video - SourceSet", description = "소스셋 관리 API")
@RestController
@RequestMapping("/video/source-sets")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class SourceSetController {

    private final SourceSetService sourceSetService;

    @PostMapping
    @Operation(
        summary = "소스셋 생성(문서 묶기) (프론트 -> 백엔드)",
        description = "여러 문서를 하나의 영상 제작 단위(SourceSet)로 묶습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공",
            content = @Content(schema = @Schema(implementation = SourceSetCreateResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "생성 실패")
    })
    public ResponseEntity<SourceSetCreateResponse> createSourceSet(
        @Valid @RequestBody SourceSetCreateRequest req,
        @AuthenticationPrincipal Jwt jwt
    ) {
        // JWT에서 사용자 UUID 추출
        UUID userUuid = SecurityUtils.extractUserUuid(jwt)
            .orElseThrow(() -> new IllegalArgumentException("사용자 UUID를 추출할 수 없습니다."));
        
        SourceSetCreateResponse response = sourceSetService.createSourceSet(req, userUuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{sourceSetId}")
    @Operation(
        summary = "소스셋 문서 변경(추가/제거) (프론트 -> 백엔드)",
        description = "소스셋의 문서 목록을 수정합니다. 단, LOCKED 이후는 금지됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "변경 성공",
            content = @Content(schema = @Schema(implementation = SourceSetCreateResponse.class))),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "소스셋/문서 없음"),
        @ApiResponse(responseCode = "409", description = "LOCKED 상태(변경 불가)"),
        @ApiResponse(responseCode = "500", description = "변경 실패")
    })
    public ResponseEntity<SourceSetCreateResponse> updateSourceSetDocuments(
        @Parameter(description = "소스셋 ID", required = true)
        @PathVariable UUID sourceSetId,
        @Valid @RequestBody SourceSetUpdateRequest req
    ) {
        SourceSetCreateResponse response = sourceSetService.updateSourceSetDocuments(sourceSetId, req);
        return ResponseEntity.ok(response);
    }
}
