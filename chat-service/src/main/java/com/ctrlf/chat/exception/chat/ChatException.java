package com.ctrlf.chat.exception.chat;

import com.ctrlf.chat.exception.BusinessException;
import com.ctrlf.chat.exception.ErrorCode;

public class ChatException extends BusinessException {
    public ChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
