package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatSessionCreateRequest;
import com.ctrlf.chat.dto.request.ChatSessionUpdateRequest;
import com.ctrlf.chat.dto.response.ChatSessionHistoryResponse;
import com.ctrlf.chat.dto.response.ChatSessionResponse;
import java.util.List;
import java.util.UUID;

/**
 * 채팅 세션 관련 비즈니스 로직 인터페이스
 * 
 * <p>세션 생성, 조회, 수정, 삭제 등의 기능을 정의합니다.</p>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public interface ChatSessionService {

    /**
     * 세션 생성
     * 
     * @param request 세션 생성 요청
     * @return 생성된 세션 정보
     */
    ChatSessionResponse createSession(ChatSessionCreateRequest request);

    /**
     * 세션 단건 조회
     * 
     * @param sessionId 세션 ID
     * @return 세션 정보
     */
    ChatSessionResponse getSession(UUID sessionId);

    /**
     * 세션 목록 조회
     * 
     * @return 세션 목록
     */
    List<ChatSessionResponse> getSessionList();

    /**
     * 세션 수정
     * 
     * @param sessionId 세션 ID
     * @param request 수정 요청
     * @return 수정된 세션 정보
     */
    ChatSessionResponse updateSession(UUID sessionId, ChatSessionUpdateRequest request);

    /**
     * 세션 삭제 (Soft Delete)
     * 
     * @param sessionId 세션 ID
     */
    void deleteSession(UUID sessionId);

    /**
     * 세션 히스토리 조회
     * 
     * @param sessionId 세션 ID
     * @return 세션 정보 및 메시지 목록
     */
    ChatSessionHistoryResponse getSessionHistory(UUID sessionId);

    /**
     * 세션/컨텍스트 모델 설정
     *
     * <p>Frontend에서 선택한 모델을 세션에 저장합니다.</p>
     * <p>Backend는 모델 값을 검증하고 그대로 저장하며, 해석하지 않습니다.</p>
     *
     * @param sessionId 세션 ID
     * @param model 모델 식별자 ("openai" 또는 "sroberta")
     */
    void setSessionModel(UUID sessionId, String model);

    /**
     * 세션 LLM 모델 설정
     *
     * <p>관리자 대시보드에서 선택한 LLM 모델을 세션에 저장합니다.</p>
     * <p>AI 서버에서 채팅 응답 생성 시 해당 모델이 사용됩니다.</p>
     *
     * @param sessionId 세션 ID
     * @param llmModel LLM 모델 식별자 ("exaone" 또는 "openai")
     */
    void setSessionLlmModel(UUID sessionId, String llmModel);
}
