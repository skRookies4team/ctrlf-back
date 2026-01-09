package com.ctrlf.chat.strategy;

import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PrometheusClient {

    private static final String PROM_URL =
        "http://localhost:9090/api/v1/query";

    private static final RestTemplate rest = new RestTemplate();

    public static double query(String promql) {
        try {
            String encoded = URLEncoder.encode(promql, StandardCharsets.UTF_8);
            String url = PROM_URL + "?query=" + encoded;

            PromResponse res = rest.getForObject(url, PromResponse.class);

            if (res == null) return 0.0;
            return res.extractSingleValue();
        } catch (Exception e) {
            // 🔥 Prometheus 장애 시 전략 판단은 안전하게 DEFAULT
            return 0.0;
        }
    }
}
