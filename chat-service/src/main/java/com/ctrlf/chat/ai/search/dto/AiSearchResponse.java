package com.ctrlf.chat.ai.search.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiSearchResponse {

    private List<Result> results;

    @Data
    public static class Result {
        private String docId;
        private String title;
        private Integer page;
        private Double score;
        private String snippet;
        private String dataset;
        private String source;
    }
}
