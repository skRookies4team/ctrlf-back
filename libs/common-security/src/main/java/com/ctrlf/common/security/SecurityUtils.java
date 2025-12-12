package com.ctrlf.common.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * 보안 관련 공통 유틸.
 * 각 서비스에서 JWT의 subject를 UUID로 파싱할 때 사용합니다.
 */
public final class SecurityUtils {
    private SecurityUtils() {}

    /**
     * JWT에서 subject를 읽어 UUID로 변환합니다.
     * - subject가 비어있거나 UUID 형식이 아니면 Optional.empty()를 반환합니다.
     */
    public static Optional<UUID> extractUserUuid(Jwt jwt) {
        if (jwt == null) return Optional.empty();
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(sub));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

