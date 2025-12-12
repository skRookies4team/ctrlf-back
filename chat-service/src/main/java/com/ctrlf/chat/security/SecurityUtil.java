package com.ctrlf.chat.security;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtil {

    public static UUID getUserUuid() {
        Object principal =
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Jwt jwt) {
            return UUID.fromString(jwt.getSubject()); // ✅ sub 값 → userUuid
        }

        throw new IllegalStateException("인증된 사용자 정보가 없습니다.");
    }
}
