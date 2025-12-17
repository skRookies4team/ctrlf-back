package com.ctrlf.infra.rag.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * AI 서버(RAG 파이프라인) 호출 클라이언트.
 *
 * - POST {baseUrl}/ai/rag/process
 * - 문서 인덱싱(다운로드/추출/청킹/임베딩/색인)을 요청합니다.
 */
@Component
public class RagAiClient {
    private static final Logger log = LoggerFactory.getLogger(RagAiClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String internalToken;
    private final Duration timeout;

    public RagAiClient(
        @Value("${app.rag.ai.base-url:http://localhost:8000}") String baseUrl,
        @Value("${app.rag.ai.token:}") String internalToken,
        @Value("${app.rag.ai.timeout-seconds:60}") long timeoutSeconds
    ) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        // HTTP/1.1 강제: JDK HttpClient 사용
        HttpClient jdk = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
        this.restClient = RestClient.builder()
            .baseUrl(normalized)
            .requestFactory(new JdkClientHttpRequestFactory(jdk))
            .build();
        this.internalToken = internalToken == null ? "" : internalToken;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private static String mask(String s) {
        if (s == null || s.isEmpty()) return "";
        if (s.length() <= 8) return "****";
        return s.substring(0, 4) + "****" + s.substring(s.length() - 4);
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }

    /**
     * 문서 인덱싱 요청.
     * body:
     * {
     *   "doc_id": "...",
     *   "file_url": "https://.../file.pdf",
     *   "domain": "POLICY",
     *   "acl": { "roles": [...], "departments": [...] }
     * }
     */
    public RagProcessResult process(String docId, String fileUrl, String domain, RagAcl acl) throws Exception {
        String path = "/ai/rag/process";
        Map<String, Object> body = new HashMap<>();
        body.put("doc_id", docId);
        body.put("file_url", fileUrl);
        body.put("domain", domain);
        if (acl != null) {
            Map<String, Object> aclMap = new HashMap<>();
            if (acl.getRoles() != null && !acl.getRoles().isEmpty()) {
                aclMap.put("roles", acl.getRoles());
            }
            if (acl.getDepartments() != null && !acl.getDepartments().isEmpty()) {
                aclMap.put("departments", acl.getDepartments());
            }
            if (!aclMap.isEmpty()) {
                body.put("acl", aclMap);
            }
        }
        String jsonString = objectMapper.writeValueAsString(body);
        log.info("AI POST {} (timeout={}s)", path, this.timeout.toSeconds());
        if (log.isDebugEnabled()) {
            log.debug("AI request headers: Content-Type=application/json; X-Internal-Token={}", mask(internalToken));
            log.debug("AI request payload: {}", truncate(jsonString, 2000));
        }
        try {
            RagProcessResult result = restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    if (!internalToken.isEmpty()) {
                        h.set("X-Internal-Token", internalToken);
                    }
                })
                .body(body)
                .retrieve()
                .body(RagProcessResult.class);
            if (result == null) {
                RagProcessResult r = new RagProcessResult();
                r.setDocId(docId);
                r.setSuccess(Boolean.FALSE);
                r.setMessage("Empty response from AI");
                return r;
            }
            return result;
        } catch (RestClientResponseException ex) {
            String payload = ex.getResponseBodyAsString();
            log.warn("AI process call failed. code={} body={}", ex.getRawStatusCode(), truncate(payload, 2000));
            // 422 + body missing hint → wrap and retry once
            if (ex.getRawStatusCode() == 422 && payload != null && payload.contains("\"loc\":[\"body\"]")) {
                log.info("AI 422 received with body-missing hint. Retrying with wrapped body...");
                Map<String, Object> wrapped = new HashMap<>();
                wrapped.put("body", body);
                try {
                    return restClient.post()
                        .uri(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(h -> {
                            if (!internalToken.isEmpty()) {
                                h.set("X-Internal-Token", internalToken);
                            }
                        })
                        .body(wrapped)
                        .retrieve()
                        .body(RagProcessResult.class);
                } catch (RestClientResponseException ex2) {
                    throw new RuntimeException("AI process call retry failed: " + ex2.getRawStatusCode() + " body=" + ex2.getResponseBodyAsString());
                }
            }
            throw new RuntimeException("AI process call failed with status " + ex.getRawStatusCode() + " body=" + payload);
        }
    }

    // (오버로드 및 AiResponse 제거: 서비스가 신규 process 시그니처를 직접 사용)

    /** AI 서버 응답 모델 (doc_id, success, message) */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RagProcessResult {
        @JsonProperty("doc_id")
        private String docId;
        private Boolean success;
        private String message;
        public RagProcessResult() {}
        public String getDocId() { return docId; }
        public void setDocId(String docId) { this.docId = docId; }
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /** 접근 제어(옵션) */
    public static class RagAcl {
        private java.util.List<String> roles;
        private java.util.List<String> departments;
        public RagAcl() {}
        public RagAcl(java.util.List<String> roles, java.util.List<String> departments) {
            this.roles = roles; this.departments = departments;
        }
        public java.util.List<String> getRoles() { return roles; }
        public void setRoles(java.util.List<String> roles) { this.roles = roles; }
        public java.util.List<String> getDepartments() { return departments; }
        public void setDepartments(java.util.List<String> departments) { this.departments = departments; }
    }
}

