package com.ctrlf.education.video.client;

import com.ctrlf.education.video.client.SourceSetAiDtos.StartRequest;
import com.ctrlf.education.video.client.SourceSetAiDtos.StartResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 소스셋 AI 서버 호출 클라이언트 (RestClient 방식).
 * 
 * <p>기능:
 * <ul>
 *   <li>소스셋 작업 시작 요청 (RAGFlow 적재 + 스크립트 생성)</li>
 * </ul>
 * 
 * <p>엔드포인트:
 * <ul>
 *   <li>POST /internal/ai/source-sets/{sourceSetId}/start - 소스셋 작업 시작</li>
 * </ul>
 */
@Component
public class SourceSetAiClient {

    private static final Logger log = LoggerFactory.getLogger(SourceSetAiClient.class);
    
    private final RestClient restClient;
    private final String baseUrl;

    /**
     * RestClient를 구성하여 초기화합니다.
     * 
     * @param baseUrl AI 서버 베이스 URL
     * @param internalToken 내부 인증 토큰(옵션)
     */
    public SourceSetAiClient(
        @Value("${app.video.ai.base-url:http://localhost:8000}") String baseUrl,
        @Value("${app.video.ai.token:}") String internalToken
    ) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        RestClient.Builder builder = RestClient.builder()
            .baseUrl(this.baseUrl);
        
        // 내부 토큰이 있으면 헤더 추가
        if (internalToken != null && !internalToken.isBlank()) {
            builder.defaultHeader("X-Internal-Token", internalToken);
        }
        
        this.restClient = builder.build();
        log.info("SourceSetAiClient 초기화 완료: baseUrl={}", this.baseUrl);
    }

    /**
     * 소스셋 작업 시작 요청.
     * 
     * @param sourceSetId 소스셋 ID
     * @param request 시작 요청 (videoId, scriptJobId, documents 등)
     * @return 시작 응답 (received, sourceSetId, scriptJobId, status)
     * @throws RestClientException 네트워크/서버 오류 시
     */
    public StartResponse startSourceSet(String sourceSetId, StartRequest request) {
        String uri = "/internal/ai/source-sets/" + sourceSetId + "/start";
        String fullUrl = baseUrl + uri;
        log.info("FastAPI 요청: POST {} (sourceSetId={})", fullUrl, sourceSetId);
        
        try {
            StartResponse response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(StartResponse.class);
            log.info("FastAPI 응답 성공: sourceSetId={}, response={}", sourceSetId, response);
            return response;
        } catch (RestClientException e) {
            log.error("FastAPI 요청 실패: POST {} (sourceSetId={}), error={}", fullUrl, sourceSetId, e.getMessage());
            throw e;
        }
    }
}
