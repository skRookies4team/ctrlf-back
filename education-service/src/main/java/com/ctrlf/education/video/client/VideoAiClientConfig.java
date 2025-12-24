package com.ctrlf.education.video.client;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * VideoAiClient Feign 설정.
 *
 * <p>기능:
 * <ul>
 *   <li>요청 인터셉터: X-Internal-Token 헤더 추가 (설정된 경우)</li>
 *   <li>에러 디코더: AI 서버 에러 응답 처리</li>
 *   <li>로깅 레벨 설정</li>
 * </ul>
 */
public class VideoAiClientConfig {

    @Value("${app.video.ai.token:}")
    private String internalToken;

    /**
     * 요청 인터셉터: 내부 토큰 헤더 추가.
     */
    @Bean
    public RequestInterceptor videoAiRequestInterceptor() {
        return requestTemplate -> {
            if (internalToken != null && !internalToken.isBlank()) {
                requestTemplate.header("X-Internal-Token", internalToken);
            }
        };
    }

    /**
     * 에러 디코더: AI 서버 응답 에러 처리.
     */
    @Bean
    public ErrorDecoder videoAiErrorDecoder() {
        return (methodKey, response) -> {
            String message = String.format("AI 서버 요청 실패: %s [%d]", methodKey, response.status());
            return new RuntimeException(message);
        };
    }

    /**
     * Feign 로깅 레벨 설정.
     * FULL: 헤더, 바디, 메타데이터 모두 로깅
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
