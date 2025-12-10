package com.ctrlf.chat.faq.exception;

import java.util.UUID;

public class FaqNotFoundException extends RuntimeException {

    public FaqNotFoundException(UUID id) {
        super("FAQ NOT FOUND : " + id);
    }
}
