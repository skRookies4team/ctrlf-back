package com.ctrlf.chat.faq.service;

import com.ctrlf.chat.faq.dto.response.FaqDraftResponse;
import java.util.List;
import java.util.UUID;

public interface FaqDraftService {

    List<FaqDraftResponse> getDrafts(String domain, String status);

    void approve(UUID draftId, UUID reviewerId, String question, String answer);

    void reject(UUID draftId, UUID reviewerId, String reason);
}
