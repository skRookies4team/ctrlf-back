package com.ctrlf.chat.ai.search.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SearchDataset {

    POLICY("policy"),
    TRAINING("training"),
    INCIDENT("incident"),
    SECURITY("security"),
    EDUCATION("education");

    private final String value;

    public static SearchDataset from(String value) {
        return Arrays.stream(values())
            .filter(d -> d.value.equals(value))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid dataset: " + value)
            );
    }
}
