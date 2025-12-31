package com.ctrlf.chat.telemetry.enums;

/**
 * 이벤트 타입 Enum
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public enum EventType {
    CHAT_TURN("CHAT_TURN"),
    FEEDBACK("FEEDBACK"),
    SECURITY("SECURITY");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EventType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (EventType eventType : EventType.values()) {
            if (eventType.value.equals(value)) {
                return eventType;
            }
        }
        return null;
    }
}

