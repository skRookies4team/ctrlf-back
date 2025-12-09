package com.ctrlf.chat.exception.chat;

public class ChatSessionNotFoundException extends RuntimeException {

    public ChatSessionNotFoundException() {
        super("채팅 세션을 찾을 수 없습니다.");
    }
}
