package com.ctrlf.education.quiz.client;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * QuizAiClient Feign 설정.
 * - X-Internal-Token 헤더 주입(옵션)
 * - 에러 디코더
 * - 로깅 레벨
 */
public class QuizAiClientConfig {

    @Value("${app.quiz.ai.token:}")
    private String internalToken;

    @Bean
    public RequestInterceptor quizAiRequestInterceptor() {
        return template -> {
            if (internalToken != null && !internalToken.isBlank()) {
                template.header("X-Internal-Token", internalToken);
            }
        };
    }

    @Bean
    public ErrorDecoder quizAiErrorDecoder() {
        return (methodKey, response) ->
            new RuntimeException("Quiz AI 요청 실패: " + methodKey + " [" + response.status() + "]");
    }

    @Bean
    public Logger.Level quizFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}

