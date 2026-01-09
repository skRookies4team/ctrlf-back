package com.ctrlf.chat.strategy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class StrategyState {

    public static final Map<String, Map<String, Object>> LAST_STRATEGY =
        new ConcurrentHashMap<>();

    public static final List<Map<String, Object>> STRATEGY_EVENTS =
        new CopyOnWriteArrayList<>();

    // ✅ 기본 전략 (🔥 이게 핵심)
    public static final Map<String, Object> DEFAULT_STRATEGY = Map.of(
        "useRag", true,
        "model", null,
        "reason", "NO_METRIC_DATA"
    );

    public static void recordEvent(
        String domain,
        Map<String, Object> oldStrategy,
        Map<String, Object> newStrategy
    ) {
        STRATEGY_EVENTS.add(
            Map.of(
                "domain", domain,
                "from", oldStrategy,
                "to", newStrategy,
                "reason", newStrategy.get("reason"),
                "timestamp", System.currentTimeMillis()
            )
        );
    }
}
