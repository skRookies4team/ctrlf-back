package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatSessionCreateRequest;
import com.ctrlf.chat.dto.request.ChatSessionUpdateRequest;
import com.ctrlf.chat.dto.response.ChatSessionHistoryResponse;
import com.ctrlf.chat.dto.response.ChatSessionResponse;
import java.util.List;
import java.util.UUID;

public interface ChatSessionService {

    ChatSessionResponse createSession(ChatSessionCreateRequest request);

    ChatSessionResponse getSession(UUID sessionId);

    List<ChatSessionResponse> getSessionList();

    ChatSessionResponse updateSession(UUID sessionId, ChatSessionUpdateRequest request);

    void deleteSession(UUID sessionId);

    ChatSessionHistoryResponse getSessionHistory(UUID sessionId);
}
