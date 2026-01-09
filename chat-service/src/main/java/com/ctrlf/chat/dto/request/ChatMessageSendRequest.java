package com.ctrlf.chat.dto.request;

import java.util.UUID;

/**
 * 메시지 전송 요청 DTO. (섹션 삭제 반영)
 * 
 * <p>Backend는 Frontend로부터 전달받은 model 값을 검증하고 AI 서비스로 그대로 전달합니다.</p>
 * <p>Backend는 모델 해석/매핑을 하지 않으며, 단순 전달자 역할만 수행합니다.</p>
 */
public record ChatMessageSendRequest(
    /** 대상 세션 ID */
    UUID sessionId,
    /** 메시지 내용 */
    String content,
    /**
     * A/B 테스트 임베딩 모델 선택 (선택)
     * - "openai": text-embedding-3-large
     * - "sroberta": ko-sroberta-multitask
     * - null: 세션에 저장된 모델 사용 (없으면 기본값 "openai")
     * 
     * <p>Backend는 이 값을 검증하고 AI 서비스로 그대로 전달합니다.</p>
     */
    String model
) {}
