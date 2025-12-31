package com.ctrlf.chat.telemetry.enums;

/**
 * 기간 Enum
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public enum Period {
    TODAY("today"),
    DAYS_7("7d"),
    DAYS_30("30d"),
    DAYS_90("90d");

    private final String value;

    Period(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Period fromString(String value) {
        if (value == null) {
            return DAYS_30; // 기본값
        }
        for (Period period : Period.values()) {
            if (period.value.equals(value)) {
                return period;
            }
        }
        return DAYS_30; // 기본값
    }

    /**
     * Period를 일수로 변환
     */
    public int toDays() {
        return switch (this) {
            case TODAY -> 0;
            case DAYS_7 -> 7;
            case DAYS_30 -> 30;
            case DAYS_90 -> 90;
        };
    }
}

