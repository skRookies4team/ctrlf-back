package com.ctrlf.chat.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ✅ 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청입니다."),

    // ✅ 채팅 도메인
    CHAT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅 세션을 찾을 수 없습니다."),
    CHAT_SECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_002", "채팅 섹션을 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_003", "채팅 메시지를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
