package com.ctrlf.chat.strategy;

import java.time.LocalDateTime;

public class StrategyEvent {

    private final String domain;
    private final String fromStrategy;
    private final String toStrategy;
    private final String reason;
    private final LocalDateTime occurredAt;

    public StrategyEvent(
        String domain,
        String fromStrategy,
        String toStrategy,
        String reason,
        LocalDateTime occurredAt
    ) {
        this.domain = domain;
        this.fromStrategy = fromStrategy;
        this.toStrategy = toStrategy;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }

    public String getDomain() {
        return domain;
    }

    public String getFromStrategy() {
        return fromStrategy;
    }

    public String getToStrategy() {
        return toStrategy;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
