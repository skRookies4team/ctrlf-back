package com.ctrlf.education.quiz.client;

import com.ctrlf.education.quiz.client.QuizAiDtos.GenerateRequest;
import com.ctrlf.education.quiz.client.QuizAiDtos.GenerateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "quiz-ai-client",
    url = "${app.quiz.ai.base-url:http://localhost:8000}",
    configuration = QuizAiClientConfig.class
)
public interface QuizAiClient {

    /**
     * 퀴즈 문항 자동 생성 요청.
     * 기본 경로는 /ai/quiz/generate 로 가정합니다. (문서 기반)
     */
    @PostMapping("/ai/quiz/generate")
    GenerateResponse generate(@RequestBody GenerateRequest request);
}

