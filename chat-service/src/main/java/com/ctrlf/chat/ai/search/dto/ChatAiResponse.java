package com.ctrlf.chat.ai.search.dto;

import com.ctrlf.chat.dto.response.ChatActionDto;
import com.ctrlf.chat.dto.response.ChatSourceDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatAiResponse {

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("model")
    private String model;

    /** RAG 참조 문서 목록 (출처 정보) */
    @JsonProperty("sources")
    private List<ChatSourceDto> sources;

    @JsonProperty("meta")
    private Meta meta;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        @JsonProperty("route")
        private String route;

        @JsonProperty("masked")
        private Boolean masked;

        /** 프론트엔드 액션 지시 (영상 재생, 퀴즈 시작 등) */
        @JsonProperty("action")
        private ChatActionDto action;
    }
}
