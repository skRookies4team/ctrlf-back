package com.ctrlf.chat.exception.chat;

import com.ctrlf.chat.exception.ErrorCode;

public class ChatSessionNotFoundException extends ChatException {

    public ChatSessionNotFoundException() {
        super(ErrorCode.CHAT_SESSION_NOT_FOUND);
    }
}
