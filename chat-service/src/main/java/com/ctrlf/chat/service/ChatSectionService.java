package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatSectionCreateRequest;
import com.ctrlf.chat.dto.response.ChatSectionResponse;

import java.util.List;
import java.util.UUID;

public interface ChatSectionService {

    // ✅ 섹션 생성
    ChatSectionResponse createSection(UUID sessionId, ChatSectionCreateRequest request);

    // ✅ 섹션 목록 조회
    List<ChatSectionResponse> getSectionList(UUID sessionId);

    // ✅ 섹션 종료
    void closeSection(UUID sessionId, UUID sectionId);
}
