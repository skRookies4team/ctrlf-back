package com.ctrlf.chat.ai.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatAiResponse {

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("model")
    private String model;

    @JsonProperty("meta")
    private Meta meta;

    @Getter
    @NoArgsConstructor
    public static class Meta {
        @JsonProperty("route")
        private String route;

        @JsonProperty("masked")
        private Boolean masked;
    }
}
