package com.ctrlf.infra.telemetry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 내부 API 토큰 설정
 *
 * <p>AI 서버 → 백엔드 내부 API 인증에 사용되는 토큰 설정입니다.</p>
 *
 * <pre>
 * app:
 *   internal:
 *     token: ${INTERNAL_API_TOKEN:dev-internal-token}
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "app.internal")
public class InternalTokenProperties {

    /**
     * 내부 API 인증 토큰
     *
     * <p>AI 서버의 BACKEND_INTERNAL_TOKEN과 동일한 값으로 설정해야 합니다.</p>
     */
    private String token = "dev-internal-token";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
