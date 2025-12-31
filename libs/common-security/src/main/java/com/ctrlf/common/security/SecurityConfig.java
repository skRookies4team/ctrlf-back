package com.ctrlf.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private InternalTokenAuthenticationFilter internalTokenFilter;

    @Bean
    @ConditionalOnMissingBean(name = "securityFilterChain")
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        Environment env
    ) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        
        // 내부 토큰 필터 추가 (있으면 자동으로 추가)
        if (internalTokenFilter != null) {
            http.addFilterBefore(internalTokenFilter, UsernamePasswordAuthenticationFilter.class);
        }
        
        // API Gateway에서 이미 인증을 검증하므로, 모든 요청을 허용
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/internal/**").authenticated()  // /internal/**는 내부 토큰 인증 필요
            .anyRequest().permitAll()
        );
        http.httpBasic(b -> b.disable());
        http.formLogin(f -> f.disable());

        // OAuth2 Resource Server 활성화 (JWT 파싱용)
        // issuer-uri가 설정되어 있으면 JWT를 파싱하여 @AuthenticationPrincipal에 주입
        String issuer = env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri");
        String jwk = env.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
        if ((issuer != null && !issuer.isBlank()) || (jwk != null && !jwk.isBlank())) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        }
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}


