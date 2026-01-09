package com.ctrlf.chat.strategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class StrategyState {

    // 도메인별 마지막 전략
    public static final Map<String, Map<String, Object>> LAST_STRATEGY =
        new ConcurrentHashMap<>();

    // 전략 변경 이벤트
    private static final List<StrategyEvent> EVENTS =
        new CopyOnWriteArrayList<>();

    // 기본 전략 (🔥 Map.copyOf 사용 금지)
    public static final Map<String, Object> DEFAULT_STRATEGY;

    static {
        Map<String, Object> m = new ConcurrentHashMap<>();
        m.put("useRag", true);
        m.put("model", "DEFAULT"); // ❗ null 절대 금지
        m.put("reason", "NO_METRIC_DATA");
        DEFAULT_STRATEGY = m;      // 그대로 할당
    }

    public static void recordEvent(
        String domain,
        Map<String, Object> oldStrategy,
        Map<String, Object> newStrategy
    ) {
        String fromReason =
            oldStrategy == null
                ? "NONE"
                : String.valueOf(oldStrategy.getOrDefault("reason", "UNKNOWN"));

        String toReason =
            String.valueOf(newStrategy.get("reason"));

        StrategyEvent event = new StrategyEvent(
            domain,
            fromReason,
            toReason,
            toReason,
            LocalDateTime.now()
        );

        EVENTS.add(event);

        if (EVENTS.size() > 200) {
            EVENTS.remove(0);
        }
    }

    public static List<StrategyEvent> getEvents() {
        return List.copyOf(EVENTS);
    }
}
