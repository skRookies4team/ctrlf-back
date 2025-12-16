package com.ctrlf.chat.faq.service;

import com.ctrlf.chat.faq.dto.response.FaqCandidateResponse;
import java.util.List;
import java.util.UUID;

public interface FaqCandidateService {

    List<FaqCandidateResponse> getCandidates(String domain, String status);

    FaqCandidateResponse getCandidate(UUID id);

    void excludeCandidate(UUID id, String reason);
}
