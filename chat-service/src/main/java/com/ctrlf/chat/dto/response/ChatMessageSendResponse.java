package com.ctrlf.chat.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 메시지 전송 응답 DTO.
 */
public record ChatMessageSendResponse(
    /** 생성된(보통 assistant) 메시지 ID */
    UUID messageId,
    /** 메시지 역할(user/assistant) */
    String role,
    /** 메시지 내용 */
    String content,
    /** 생성 시각 */
    Instant createdAt,
    /** RAG 참조 문서 목록 (출처 정보) - 프론트엔드에서 "[참고 근거]" 섹션으로 표시 */
    List<ChatSourceDto> sources,
    /** 프론트엔드 액션 지시 (예: 교육 영상 이어보기 요청 시 자동 재생) */
    ChatActionDto action
) {
    /**
     * 기존 호환용 생성자 (sources, action 없이 생성).
     */
    public ChatMessageSendResponse(UUID messageId, String role, String content, Instant createdAt) {
        this(messageId, role, content, createdAt, null, null);
    }
}
