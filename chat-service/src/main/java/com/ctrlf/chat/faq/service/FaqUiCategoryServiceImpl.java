package com.ctrlf.chat.faq.service;

import com.ctrlf.chat.faq.dto.request.FaqUiCategoryCreateRequest;
import com.ctrlf.chat.faq.dto.request.FaqUiCategoryUpdateRequest;
import com.ctrlf.chat.faq.dto.response.FaqUiCategoryResponse;
import com.ctrlf.chat.faq.entity.Faq;
import com.ctrlf.chat.faq.entity.FaqRevision;
import com.ctrlf.chat.faq.entity.FaqUiCategory;
import com.ctrlf.chat.faq.repository.FaqRepository;
import com.ctrlf.chat.faq.repository.FaqRevisionRepository;
import com.ctrlf.chat.faq.repository.FaqUiCategoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FaqUiCategoryServiceImpl implements FaqUiCategoryService {

    private static final String ETC_SLUG = "ETC";

    private final FaqUiCategoryRepository faqUiCategoryRepository;
    private final FaqRepository faqRepository;
    private final FaqRevisionRepository faqRevisionRepository;

    @Override
    public UUID create(FaqUiCategoryCreateRequest req, UUID operatorId) {
        if (req.getSlug() == null || req.getSlug().isBlank()) {
            throw new IllegalArgumentException("slug는 필수입니다.");
        }
        if (req.getDisplayName() == null || req.getDisplayName().isBlank()) {
            throw new IllegalArgumentException("displayName은 필수입니다.");
        }

        faqUiCategoryRepository.findBySlug(req.getSlug())
            .ifPresent(x -> { throw new IllegalArgumentException("이미 존재하는 slug 입니다."); });

        int sortOrder = (req.getSortOrder() != null) ? req.getSortOrder() : 999;

        FaqUiCategory category = FaqUiCategory.create(
            UUID.randomUUID(),
            req.getSlug(),
            req.getDisplayName(),
            sortOrder,
            operatorId
        );

        return faqUiCategoryRepository.save(category).getId();
    }

    @Override
    public void update(UUID categoryId, FaqUiCategoryUpdateRequest req, UUID operatorId) {
        log.info("UI 카테고리 수정 시작: categoryId={}, operatorId={}, displayName={}, sortOrder={}, isActive={}",
            categoryId, operatorId, req.getDisplayName(), req.getSortOrder(), req.getIsActive());
        
        try {
            FaqUiCategory category = faqUiCategoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("카테고리를 찾을 수 없습니다: categoryId={}", categoryId);
                    return new IllegalArgumentException(
                        String.format("카테고리가 존재하지 않습니다. categoryId=%s", categoryId)
                    );
                });

            log.debug("카테고리 현재 상태: categoryId={}, slug={}, displayName={}, sortOrder={}, isActive={}",
                categoryId, category.getSlug(), category.getDisplayName(), 
                category.getSortOrder(), category.getIsActive());

            category.update(req.getDisplayName(), req.getSortOrder(), req.getIsActive(), operatorId);
            
            // 명시적으로 저장 (트랜잭션 커밋 시 자동 저장되지만, 명시적으로 호출)
            faqUiCategoryRepository.save(category);
            
            log.info("UI 카테고리 수정 완료: categoryId={}", categoryId);
        } catch (DataIntegrityViolationException e) {
            log.error("DB 제약 조건 위반: categoryId={}, error={}", categoryId, e.getMessage(), e);
            throw new IllegalStateException(
                String.format("카테고리 업데이트 중 DB 제약 조건 위반이 발생했습니다. categoryId=%s, error=%s",
                    categoryId, e.getMessage()),
                e
            );
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException은 그대로 전파 (GlobalExceptionHandler에서 400으로 처리)
            log.error("잘못된 인자: categoryId={}, error={}", categoryId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("카테고리 업데이트 실패: categoryId={}, error={}", categoryId, e.getMessage(), e);
            throw new IllegalStateException(
                String.format("카테고리 업데이트 중 오류가 발생했습니다. categoryId=%s, error=%s",
                    categoryId, e.getMessage()),
                e
            );
        }
    }

    // ===============================
    // ✅ 카테고리 비활성화 + FAQ 이동
    // ===============================
    @Override
    public void deactivate(UUID categoryId, UUID operatorId, String reason) {
        FaqUiCategory category = faqUiCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다."));

        if (!category.getIsActive()) return;

        FaqUiCategory etc = faqUiCategoryRepository.findBySlug(ETC_SLUG)
            .orElseThrow(() -> new IllegalStateException("ETC 카테고리가 존재하지 않습니다."));

        category.setIsActive(false);

        List<Faq> faqs = faqRepository.findByUiCategoryId(categoryId);
        for (Faq faq : faqs) {
            faq.setUiCategoryId(etc.getId());
            faq.setNeedsRecategorization(true);
        }

        faqRevisionRepository.save(
            FaqRevision.create(
                "UI_CATEGORY",
                categoryId,
                "DEACTIVATE",
                operatorId,
                reason
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqUiCategoryResponse> getActiveCategories() {
        return faqUiCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
            .stream().map(FaqUiCategoryResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqUiCategoryResponse> getAllCategories() {
        return faqUiCategoryRepository.findAllByOrderBySortOrderAsc()
            .stream().map(FaqUiCategoryResponse::from).toList();
    }
}
