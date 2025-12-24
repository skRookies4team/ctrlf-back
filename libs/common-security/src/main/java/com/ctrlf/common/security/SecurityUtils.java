package com.ctrlf.common.security;

import java.util.Collections;
import java.util.List;
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

    /**
     * JWT에서 사용자 부서 목록을 추출합니다.
     * - department 클레임이 없거나 비어있으면 빈 리스트를 반환합니다.
     */
    @SuppressWarnings("unchecked")
    public static List<String> extractDepartments(Jwt jwt) {
        if (jwt == null) return Collections.emptyList();
        Object deptClaim = jwt.getClaim("department");
        if (deptClaim == null) return Collections.emptyList();
        if (deptClaim instanceof List) {
            return (List<String>) deptClaim;
        }
        if (deptClaim instanceof String) {
            String dept = (String) deptClaim;
            return dept.isBlank() ? Collections.emptyList() : List.of(dept);
        }
        return Collections.emptyList();
    }
}

