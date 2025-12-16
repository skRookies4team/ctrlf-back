package com.ctrlf.chat.ai.search.dto;

import lombok.Data;

@Data
public class AiSearchRequest {

    private String query;
    private Integer topK = 5;
    private String dataset;
}
