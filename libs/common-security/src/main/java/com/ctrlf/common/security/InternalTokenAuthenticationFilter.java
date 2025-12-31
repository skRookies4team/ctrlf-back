package com.ctrlf.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 내부 토큰 인증 필터 (공통)
 * 
 * <p>모든 서비스(chat, education, infra)에서 공통으로 사용하는 내부 토큰 인증 필터입니다.</p>
 * <p>/internal/** 경로에 대해 X-Internal-Token 헤더를 검증합니다.</p>
 * 
 * <p>사용 방법:</p>
 * <pre>
 * {@code
 * @Configuration
 * public class SecurityConfig {
 *     @Bean
 *     public SecurityFilterChain filterChain(
 *         HttpSecurity http,
 *         InternalTokenAuthenticationFilter internalTokenFilter
 *     ) {
 *         http.addFilterBefore(
 *             internalTokenFilter,
 *             UsernamePasswordAuthenticationFilter.class
 *         );
 *         return http.build();
 *     }
 * }
 * }
 * </pre>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class InternalTokenAuthenticationFilter extends OncePerRequestFilter {

    private final InternalTokenProperties tokenProperties;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        // /internal/** 경로만 체크
        if (path.startsWith("/internal/")) {
            String token = request.getHeader("X-Internal-Token");

            if (token == null || token.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"X-Internal-Token header is required\"}");
                return;
            }

            if (!tokenProperties.isValid() || !tokenProperties.getToken().equals(token)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Invalid X-Internal-Token\"}");
                return;
            }

            // 인증 성공 - 내부 서비스 권한 부여
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

