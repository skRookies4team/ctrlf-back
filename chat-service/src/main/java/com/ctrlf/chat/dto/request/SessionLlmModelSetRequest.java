package com.ctrlf.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 세션 LLM 모델 설정 요청 DTO
 *
 * <p>관리자 대시보드에서 선택한 LLM 모델을 세션에 설정합니다.</p>
 * <p>AI 서버에서 채팅 응답 생성 시 해당 모델이 사용됩니다.</p>
 */
public record SessionLlmModelSetRequest(
    /**
     * LLM 모델 선택 (관리자 대시보드에서 설정)
     * - "exaone": 내부 EXAONE LLM
     * - "openai": OpenAI GPT
     */
    @NotBlank(message = "llmModel은 필수입니다")
    @Pattern(regexp = "^(exaone|openai)$", message = "llmModel은 'exaone' 또는 'openai'만 허용됩니다")
    String llmModel
) {}
