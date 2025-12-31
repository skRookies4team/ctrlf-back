package com.ctrlf.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 내부 서비스 간 통신용 토큰 속성 (공통)
 * 
 * <p>모든 서비스(chat, education, infra)에서 공통으로 사용하는 INTERNAL_TOKEN을 관리합니다.</p>
 * <p>환경변수 INTERNAL_TOKEN을 우선 사용하고, 없으면 application.yml의 app.internal.token을 사용합니다.</p>
 * 
 * <p>설정 방법:</p>
 * <ul>
 *   <li>환경변수: export INTERNAL_TOKEN="your-token"</li>
 *   <li>Docker: -e INTERNAL_TOKEN="your-token"</li>
 *   <li>Kubernetes: Secret 또는 ConfigMap</li>
 * </ul>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "app.internal")
public class InternalTokenProperties {

    /**
     * 내부 서비스 간 통신용 토큰
     * 우선순위: 환경변수 INTERNAL_TOKEN > application.yml의 app.internal.token
     */
    @Value("${INTERNAL_TOKEN:${app.internal.token:}}")
    private String token = "";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 토큰이 유효한지 확인
     */
    public boolean isValid() {
        return token != null && !token.isBlank();
    }
}

