package com.ctrlf.education.quiz.client;

import com.ctrlf.education.quiz.client.QuizAiDtos.GenerateRequest;
import com.ctrlf.education.quiz.client.QuizAiDtos.GenerateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * 퀴즈 AI 서버 호출 클라이언트 (RestClient 방식).
 * 
 * <p>기능:
 * <ul>
 *   <li>퀴즈 문항 자동 생성 요청</li>
 * </ul>
 * 
 * <p>엔드포인트:
 * <ul>
 *   <li>POST /ai/quiz/generate - 퀴즈 문항 생성</li>
 * </ul>
 */
@Component
public class QuizAiClient {

    private final RestClient restClient;

    /**
     * RestClient를 구성하여 초기화합니다.
     * 
     * @param baseUrl AI 서버 베이스 URL
     * @param internalToken 내부 인증 토큰(옵션)
     */
    public QuizAiClient(
        @Value("${app.quiz.ai.base-url:http://localhost:8000}") String baseUrl,
        @Value("${app.quiz.ai.token:}") String internalToken
    ) {
        // HTTP/1.1을 강제하기 위해 SimpleClientHttpRequestFactory 사용
        // FastAPI/Uvicorn은 HTTP/2를 지원하지 않으므로 HTTP/1.1을 사용해야 함
        // SimpleClientHttpRequestFactory는 HTTP/1.1만 지원하므로 HTTP/2 업그레이드 시도를 방지
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000);  // 연결 타임아웃: 30초
        requestFactory.setReadTimeout(600000);     // 읽기 타임아웃: 120초 (퀴즈 생성은 LLM 호출로 시간이 걸림)
        
        RestClient.Builder builder = RestClient.builder()
            .baseUrl(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl)
            .requestFactory(requestFactory);
        
        // 내부 토큰이 있으면 헤더 추가
        if (internalToken != null && !internalToken.isBlank()) {
            builder.defaultHeader("X-Internal-Token", internalToken);
        }
        
        this.restClient = builder.build();
    }

    /**
     * 퀴즈 문항 자동 생성 요청.
     * 
     * @param request 생성 요청 (educationId, attemptNo, language, numQuestions, questionType)
     * @return 생성된 문항 목록
     * @throws org.springframework.web.client.RestClientException 네트워크/서버 오류 시
     */
    public GenerateResponse generate(GenerateRequest request) {
        return restClient.post()
            .uri("/ai/quiz/generate")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(GenerateResponse.class);
    }
}
