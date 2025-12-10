package com.ctrlf.chat.exception.chat;

import com.ctrlf.chat.exception.ErrorCode;

public class ChatSectionNotFoundException extends ChatException {

    public ChatSectionNotFoundException() {
        super(ErrorCode.CHAT_SECTION_NOT_FOUND);
    }
}
