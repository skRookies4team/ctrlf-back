package com.ctrlf.chat.ai.search.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatAiResponse {

    private String answer;
    private Integer promptTokens;
    private Integer completionTokens;
    private String model;
}
