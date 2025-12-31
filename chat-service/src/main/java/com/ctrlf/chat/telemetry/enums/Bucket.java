package com.ctrlf.chat.telemetry.enums;

/**
 * 버킷 Enum (집계 단위)
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public enum Bucket {
    DAY("day"),
    WEEK("week");

    private final String value;

    Bucket(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Bucket fromString(String value) {
        if (value == null) {
            return WEEK; // 기본값
        }
        for (Bucket bucket : Bucket.values()) {
            if (bucket.value.equals(value)) {
                return bucket;
            }
        }
        return WEEK; // 기본값
    }
}

