package com.ctrlf.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 세션/컨텍스트 모델 설정 요청 DTO
 * 
 * <p>Frontend에서 선택한 모델을 세션에 설정합니다.</p>
 * <p>이 요청은 질문과 무관하게 세션/컨텍스트를 설정합니다.</p>
 */
public record SessionModelSetRequest(
    /**
     * A/B 테스트 임베딩 모델 선택
     * - "openai": text-embedding-3-large
     * - "sroberta": ko-sroberta-multitask
     */
    @NotBlank(message = "model은 필수입니다")
    @Pattern(regexp = "^(openai|sroberta)$", message = "model은 'openai' 또는 'sroberta'만 허용됩니다")
    String model
) {}

