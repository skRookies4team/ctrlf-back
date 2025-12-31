package com.ctrlf.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * 내부 토큰 인증 필터 자동 설정
 * 
 * <p>common-security를 사용하는 모든 서비스(chat, education, infra)에서 자동으로 
 * InternalTokenAuthenticationFilter를 등록합니다.</p>
 * <p>InternalTokenProperties가 있으면 자동으로 필터를 생성합니다.</p>
 * <p>각 서비스의 SecurityConfig에서 이 필터를 주입받아 SecurityFilterChain에 추가해야 합니다.</p>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(InternalTokenProperties.class)
public class InternalTokenFilterConfig {

    @Bean
    public InternalTokenAuthenticationFilter internalTokenAuthenticationFilter(
        InternalTokenProperties tokenProperties
    ) {
        return new InternalTokenAuthenticationFilter(tokenProperties);
    }
}

