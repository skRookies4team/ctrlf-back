package com.ctrlf.chat.ai.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatAiRequest {

    @JsonProperty("session_id")
    private UUID sessionId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("user_role")
    private String userRole;

    @JsonProperty("department")
    private String department;

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("messages")
    private List<ChatAiMessage> messages;

    // A/B 테스트 임베딩 모델 선택 (선택)
    // "openai" (text-embedding-3-large) 또는 "sroberta" (ko-sroberta-multitask)
    // null인 경우 기본값(openai) 사용
    @JsonProperty("model")
    private String model;

    // LLM 모델 선택 (관리자 대시보드에서 설정)
    // "exaone" (내부 EXAONE) 또는 "openai" (GPT)
    // null인 경우 AI 서버 기본값(exaone) 사용
    @JsonProperty("llm_model")
    private String llmModel;
}
