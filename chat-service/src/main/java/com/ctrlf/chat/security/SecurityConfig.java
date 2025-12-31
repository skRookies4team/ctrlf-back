package com.ctrlf.chat.security;

import com.ctrlf.common.security.ApiRoles;
import com.ctrlf.common.security.InternalTokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 * 
 * <p>JWT 기반 인증 및 엔드포인트별 접근 권한을 설정합니다.</p>
 * 
 * <ul>
 *   <li>/actuator/** : 인증 불필요 (헬스체크 등)</li>
 *   <li>/internal/** : X-Internal-Token 헤더 인증 (공통 라이브러리 사용)</li>
 *   <li>/admin/** : JWT 인증 + SYSTEM_ADMIN 권한 필요</li>
 *   <li>/api/** : 인증 필요 (JWT 토큰 필수)</li>
 *   <li>기타 경로 : 인증 불필요</li>
 * </ul>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private InternalTokenAuthenticationFilter internalTokenFilter;

    /**
     * Security Filter Chain 설정
     * 
     * @param http HttpSecurity 객체
     * @return 설정된 SecurityFilterChain
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 비활성화 (REST API이므로 불필요)
            .csrf(AbstractHttpConfigurer::disable)
            // 세션 사용 안 함 (JWT 기반)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        // 내부 토큰 인증 필터 추가 (있으면 추가, 공통 라이브러리 사용)
        if (internalTokenFilter != null) {
            http.addFilterBefore(internalTokenFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        http
            // 엔드포인트별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // Actuator 엔드포인트는 인증 불필요
                .requestMatchers("/actuator/**").permitAll()
                // /internal/** 경로는 내부 토큰 인증 (필터에서 처리)
                .requestMatchers("/internal/**").authenticated()
                // /admin/** 경로는 JWT 인증 + SYSTEM_ADMIN 권한 필요
                .requestMatchers("/admin/**").hasAuthority("ROLE_" + ApiRoles.SYSTEM_ADMIN)
                // /api/** 경로는 인증 필요 (JWT 토큰 필수)
                .requestMatchers("/api/**").authenticated()
                // 나머지 경로는 인증 불필요
                .anyRequest().permitAll()
            )
            // OAuth2 Resource Server 설정 (JWT 토큰 검증)
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            );

        return http.build();
    }
}
