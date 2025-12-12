package com.ctrlf.chat.faq.dto.request;

import lombok.Getter;

@Getter
public class FaqCreateRequest {

    private String question;
    private String answer;
    private String domain;
    private Integer priority;
}
