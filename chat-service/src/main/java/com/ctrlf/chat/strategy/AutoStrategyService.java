package com.ctrlf.chat.strategy;

import java.util.HashMap;
import java.util.Map;

public class AutoStrategyService {

    public static Map<String, Object> decideStrategy(String domain) {

        // ==================================================
        // 📊 Prometheus 메트릭 조회
        // ==================================================
        double avgLatency =
            PrometheusClient.query(
                "sum(rate(ctrlf_ai_request_latency_seconds_sum{domain=\"" + domain + "\"}[1m]))"
                    + " / "
                    + "sum(rate(ctrlf_ai_request_latency_seconds_count{domain=\"" + domain + "\"}[1m]))"
            );

        double ragRatio =
            PrometheusClient.query(
                "sum(rate(ctrlf_ai_requests_total{domain=\"" + domain + "\",rag_used=\"True\"}[1m]))"
                    + " / "
                    + "sum(rate(ctrlf_ai_requests_total{domain=\"" + domain + "\"}[1m]))"
            );

        // ==================================================
        // 🛡️ 안전장치 (메트릭 없을 때)
        // ==================================================
        if (Double.isNaN(avgLatency) || avgLatency == 0.0) {
            return StrategyState.DEFAULT_STRATEGY;
        }

        // ==================================================
        // 🧠 새 전략 계산
        // ==================================================
        Map<String, Object> newStrategy = new HashMap<>();

        if (avgLatency > 5.0 && ragRatio > 0.5) {
            newStrategy.put("useRag", false);
            newStrategy.put("model", "quality-gate");
            newStrategy.put("reason", "HIGH_LATENCY_HIGH_RAG");
        } else {
            newStrategy.put("useRag", true);
            newStrategy.put("model", null);
            newStrategy.put("reason", "DEFAULT");
        }

        // ==================================================
        // 🔁 변경 감지 & 이벤트 기록
        // ==================================================
        Map<String, Object> oldStrategy =
            StrategyState.LAST_STRATEGY.get(domain);

        if (!newStrategy.equals(oldStrategy)) {
            StrategyState.recordEvent(
                domain,
                oldStrategy == null ? Map.of() : oldStrategy,
                newStrategy
            );
            StrategyState.LAST_STRATEGY.put(domain, newStrategy);
        }

        return newStrategy;
    }
}
