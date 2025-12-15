package com.ctrlf.education.client;

import com.ctrlf.education.dto.VideoDtos.AiProcessResponse;
import com.ctrlf.education.dto.VideoDtos.AiVideoResponse;
import com.ctrlf.education.dto.VideoDtos.MaterialProcessRequest;
import com.ctrlf.education.dto.VideoDtos.VideoRetryRequest;
import com.ctrlf.education.dto.VideoDtos.VideoStartRequest;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI 서버(영상 생성 파이프라인) 호출 Feign 클라이언트.
 *
 * <p>기능:
 * <ul>
 *   <li>전처리 + 임베딩 + 스크립트 생성 요청</li>
 *   <li>영상 생성 시작 요청</li>
 *   <li>영상 재생성 요청</li>
 * </ul>
 *
 * <p>엔드포인트:
 * <ul>
 *   <li>POST /ai/material/{materialId}/process - 전처리/임베딩/스크립트 생성</li>
 *   <li>POST /ai/video/job/{jobId}/start - 영상 생성 시작</li>
 *   <li>POST /ai/video/job/{jobId}/retry - 영상 재생성</li>
 * </ul>
 *
 * <p>구성 (application.yml):
 * <ul>
 *   <li>app.video.ai.base-url: AI 서버 URL (기본 http://localhost:8000)</li>
 * </ul>
 */
@FeignClient(
    name = "video-ai-client",
    url = "${app.video.ai.base-url:http://localhost:8000}",
    configuration = VideoAiClientConfig.class
)
public interface VideoAiClient {

    /**
     * 전처리 + 임베딩 + 스크립트 생성 요청을 AI 서버로 전송합니다.
     *
     * @param materialId 자료 ID
     * @param request    요청 바디 (materialId, eduId, fileUrl)
     * @return AI 서버 응답 (received, status)
     */
    @PostMapping("/ai/material/{materialId}/process")
    AiProcessResponse processMaterial(
        @PathVariable("materialId") UUID materialId,
        @RequestBody MaterialProcessRequest request
    );

    /**
     * 영상 생성 시작 요청을 AI 서버로 전송합니다.
     *
     * @param jobId   Job ID
     * @param request 요청 바디 (scriptId, eduId)
     * @return AI 서버 응답 (jobId, accepted, status)
     */
    @PostMapping("/ai/video/job/{jobId}/start")
    AiVideoResponse startVideoGeneration(
        @PathVariable("jobId") UUID jobId,
        @RequestBody VideoStartRequest request
    );

    /**
     * 영상 재생성 요청을 AI 서버로 전송합니다.
     *
     * @param jobId   Job ID
     * @param request 요청 바디 (jobId, scriptId, eduId, retry)
     * @return AI 서버 응답 (jobId, accepted, status)
     */
    @PostMapping("/ai/video/job/{jobId}/retry")
    AiVideoResponse retryVideoGeneration(
        @PathVariable("jobId") UUID jobId,
        @RequestBody VideoRetryRequest request
    );
}
