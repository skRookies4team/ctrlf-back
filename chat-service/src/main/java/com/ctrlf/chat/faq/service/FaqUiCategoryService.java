package com.ctrlf.chat.faq.service;

import com.ctrlf.chat.faq.dto.request.FaqUiCategoryCreateRequest;
import com.ctrlf.chat.faq.dto.request.FaqUiCategoryUpdateRequest;
import com.ctrlf.chat.faq.dto.response.FaqUiCategoryResponse;
import java.util.List;
import java.util.UUID;

public interface FaqUiCategoryService {

    UUID create(FaqUiCategoryCreateRequest req, UUID operatorId);

    void update(UUID categoryId, FaqUiCategoryUpdateRequest req, UUID operatorId);

    // ✅ 추가
    void deactivate(UUID categoryId, UUID operatorId, String reason);

    List<FaqUiCategoryResponse> getActiveCategories();

    List<FaqUiCategoryResponse> getAllCategories();
}
