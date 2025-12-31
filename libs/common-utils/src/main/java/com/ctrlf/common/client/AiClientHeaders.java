package com.ctrlf.common.client;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * AI 서버 호출 시 필요한 공통 헤더 설정 유틸리티
 * 
 * <p>백엔드에서 AI 서버로 요청할 때 반드시 전달해야 하는 헤더:
 * <ul>
 *   <li>X-Trace-Id (필수)</li>
 *   <li>X-User-Id (필수)</li>
 *   <li>X-Dept-Id (필수)</li>
 *   <li>X-Conversation-Id (권장)</li>
 *   <li>X-Turn-Id (권장)</li>
 *   <li>X-Internal-Token (내부 호출 인증)</li>
 * </ul>
 */
public final class AiClientHeaders {
    private AiClientHeaders() {}

    /**
     * WebClient 요청에 AI 서버 호출 헤더를 추가합니다.
     * 
     * @param requestSpec WebClient 요청 스펙
     * @param traceId Trace ID (필수)
     * @param userId User ID (필수)
     * @param deptId Department ID (필수)
     * @param conversationId Conversation ID (선택)
     * @param turnId Turn ID (선택)
     * @param internalToken Internal Token (선택)
     * @return 헤더가 추가된 WebClient 요청 스펙
     */
    public static WebClient.RequestHeadersSpec<?> addHeaders(
        WebClient.RequestHeadersSpec<?> requestSpec,
        UUID traceId,
        String userId,
        String deptId,
        String conversationId,
        Integer turnId,
        String internalToken
    ) {
        requestSpec.header("X-Trace-Id", traceId != null ? traceId.toString() : "");
        requestSpec.header("X-User-Id", userId != null ? userId : "");
        requestSpec.header("X-Dept-Id", deptId != null ? deptId : "");
        
        if (conversationId != null && !conversationId.isBlank()) {
            requestSpec.header("X-Conversation-Id", conversationId);
        }
        
        if (turnId != null) {
            requestSpec.header("X-Turn-Id", turnId.toString());
        }
        
        if (internalToken != null && !internalToken.isBlank()) {
            requestSpec.header("X-Internal-Token", internalToken);
        }
        
        return requestSpec;
    }

    /**
     * JWT에서 정보를 추출하여 WebClient 요청에 헤더를 추가합니다.
     * 
     * @param requestSpec WebClient 요청 스펙
     * @param jwt JWT 토큰
     * @param traceId Trace ID (필수, JWT에서 추출 불가능하므로 별도 전달)
     * @param conversationId Conversation ID (선택)
     * @param turnId Turn ID (선택)
     * @param internalToken Internal Token (선택)
     * @return 헤더가 추가된 WebClient 요청 스펙
     */
    public static WebClient.RequestHeadersSpec<?> addHeadersFromJwt(
        WebClient.RequestHeadersSpec<?> requestSpec,
        Jwt jwt,
        UUID traceId,
        String conversationId,
        Integer turnId,
        String internalToken
    ) {
        String userId = jwt != null ? jwt.getSubject() : null;
        String deptId = extractDeptId(jwt);
        
        return addHeaders(requestSpec, traceId, userId, deptId, conversationId, turnId, internalToken);
    }

    /**
     * JWT에서 부서 ID를 추출합니다.
     * 
     * @param jwt JWT 토큰
     * @return 부서 ID (없으면 빈 문자열)
     */
    private static String extractDeptId(Jwt jwt) {
        if (jwt == null) return "";
        
        Object deptClaim = jwt.getClaim("department");
        if (deptClaim == null) return "";
        
        if (deptClaim instanceof String) {
            return (String) deptClaim;
        }
        
        if (deptClaim instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<String> deptList = (java.util.List<String>) deptClaim;
            return deptList.isEmpty() ? "" : deptList.get(0);
        }
        
        return "";
    }
}

