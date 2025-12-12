package com.ctrlf.chat.faq.service;

import com.ctrlf.chat.faq.dto.request.FaqCreateRequest;
import com.ctrlf.chat.faq.dto.request.FaqUpdateRequest;
import com.ctrlf.chat.faq.dto.response.FaqResponse;

import java.util.List;
import java.util.UUID;

public interface FaqService {

    UUID create(FaqCreateRequest request);

    void update(UUID id, FaqUpdateRequest request);

    void delete(UUID id);

    List<FaqResponse> getAll();
}
