package com.ctrlf.infra.telemetry.filter;

import com.ctrlf.infra.telemetry.config.InternalTokenProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 내부 API 토큰 검증 필터
 *
 * <p>/internal/** 경로에 대해 X-Internal-Token 헤더를 검증합니다.</p>
 *
 * <p>검증 실패 시 401 Unauthorized 응답을 반환합니다.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    private static final String INTERNAL_PATH_PREFIX = "/internal/";

    private final InternalTokenProperties tokenProperties;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        // /internal/** 경로만 검증
        if (!path.startsWith(INTERNAL_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedToken = request.getHeader(INTERNAL_TOKEN_HEADER);
        String expectedToken = tokenProperties.getToken();

        // 토큰 검증
        if (expectedToken == null || expectedToken.isBlank()) {
            // 토큰이 설정되지 않은 경우 경고만 출력하고 통과 (개발 환경 허용)
            log.warn("Internal token not configured, allowing request to {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        if (providedToken == null || providedToken.isBlank()) {
            log.warn("Missing X-Internal-Token header for path: {}", path);
            sendUnauthorizedResponse(response, "Missing X-Internal-Token header");
            return;
        }

        if (!expectedToken.equals(providedToken)) {
            log.warn("Invalid X-Internal-Token for path: {}", path);
            sendUnauthorizedResponse(response, "Invalid X-Internal-Token");
            return;
        }

        // 토큰 검증 성공
        log.debug("Internal token validated for path: {}", path);
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
            "{\"errorCode\":\"UNAUTHORIZED\",\"message\":\"%s\"}",
            message
        ));
    }
}
