package com.ctrlf.chat.ai.search.service;

import com.ctrlf.chat.ai.search.dto.AiSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiSearchService {

    private final WebClient aiWebClient;

    public Mono<AiSearchResponse> search(
        String query,
        String dataset,
        int topK
    ) {
        return aiWebClient.post()
            .uri("/search")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of(
                "query", query,
                "dataset", dataset,
                "top_k", topK
            ))
            .retrieve()
            .bodyToMono(AiSearchResponse.class)
            .timeout(Duration.ofSeconds(15));
    }
}
