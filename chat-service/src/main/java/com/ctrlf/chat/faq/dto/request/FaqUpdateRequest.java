package com.ctrlf.chat.faq.dto.request;

import lombok.Getter;

@Getter
public class FaqUpdateRequest {

    private String question;
    private String answer;
    private String domain;
    private Boolean isActive;
    private Integer priority;
}
