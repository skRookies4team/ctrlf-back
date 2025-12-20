package com.ctrlf.education.script.client;

import com.ctrlf.education.script.client.ScriptAiDtos.GenerateRequest;
import com.ctrlf.education.script.client.ScriptAiDtos.GenerateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 스크립트 AI 서버 호출 클라이언트 (RestClient 방식).
 * 
 * <p>기능:
 * <ul>
 *   <li>스크립트 자동 생성 요청</li>
 * </ul>
 * 
 * <p>엔드포인트:
 * <ul>
 *   <li>POST /api/scripts - 스크립트 자동 생성 (API_REFERENCE.md 명세)</li>
 * </ul>
 */
@Component
public class ScriptAiClient {

    private final RestClient restClient;

    /**
     * RestClient를 구성하여 초기화합니다.
     * 
     * @param baseUrl AI 서버 베이스 URL
     * @param internalToken 내부 인증 토큰(옵션)
     */
    public ScriptAiClient(
        @Value("${app.video.ai.base-url:http://localhost:8000}") String baseUrl,
        @Value("${app.video.ai.token:}") String internalToken
    ) {
        RestClient.Builder builder = RestClient.builder()
            .baseUrl(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl);
        
        // 내부 토큰이 있으면 헤더 추가
        if (internalToken != null && !internalToken.isBlank()) {
            builder.defaultHeader("X-Internal-Token", internalToken);
        }
        
        this.restClient = builder.build();
    }

    /**
     * 스크립트 자동 생성 요청.
     * 
     * @param request 생성 요청 (documentId, title, targetDurationSec, style)
     * @return 생성된 스크립트 정보 (scriptId, status, scenes, estimatedDurationSec)
     * @throws org.springframework.web.client.RestClientException 네트워크/서버 오류 시
     */
    public GenerateResponse generateScript(GenerateRequest request) {
        // RestClient는 객체를 자동으로 JSON으로 직렬화하므로 객체를 직접 전달
        return restClient.post()
            .uri("/api/scripts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(GenerateResponse.class);
    }
}
