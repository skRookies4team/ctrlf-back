package com.ctrlf.chat.faq.service;

import com.ctrlf.chat.ai.search.domain.SearchDataset;
import com.ctrlf.chat.ai.search.facade.SearchFacade;
import com.ctrlf.chat.ai.search.dto.AiSearchResponse;
import com.ctrlf.chat.faq.dto.request.FaqCreateRequest;
import com.ctrlf.chat.faq.dto.request.FaqUpdateRequest;
import com.ctrlf.chat.faq.dto.response.FaqResponse;
import com.ctrlf.chat.faq.entity.*;
import com.ctrlf.chat.faq.exception.FaqNotFoundException;
import com.ctrlf.chat.faq.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final FaqCandidateRepository faqCandidateRepository;
    private final FaqDraftRepository faqDraftRepository;
    private final FaqRevisionRepository faqRevisionRepository;
    private final FaqAiClient faqAiClient;

    // 🔹 RAG Search 연동 (이번 작업의 핵심)
    private final SearchFacade searchFacade;

    // =========================
    // 기존 FAQ CRUD
    // =========================

    @Override
    public UUID create(FaqCreateRequest request) {
        Instant now = Instant.now();
        Faq faq = new Faq();
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setDomain(request.getDomain());
        faq.setPriority(request.getPriority());
        faq.setIsActive(true);
        faq.setNeedsRecategorization(false); // 기본값 설정
        faq.setPublishedAt(now); // 기본값 설정
        faq.setCreatedAt(now);
        faq.setUpdatedAt(now);

        return faqRepository.save(faq).getId();
    }

    @Override
    public void update(UUID id, FaqUpdateRequest request) {
        Faq faq = faqRepository.findById(id)
            .orElseThrow(() -> new FaqNotFoundException(id));

        if (request.getQuestion() != null) faq.setQuestion(request.getQuestion());
        if (request.getAnswer() != null) faq.setAnswer(request.getAnswer());
        if (request.getDomain() != null) faq.setDomain(request.getDomain());
        if (request.getIsActive() != null) faq.setIsActive(request.getIsActive());
        if (request.getPriority() != null) faq.setPriority(request.getPriority());

        faq.setUpdatedAt(Instant.now());
    }

    @Override
    public void delete(UUID id) {
        Faq faq = faqRepository.findById(id)
            .orElseThrow(() -> new FaqNotFoundException(id));

        faq.setIsActive(false);
        faq.setUpdatedAt(Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqResponse> getAll() {
        return faqRepository.findByIsActiveTrueOrderByPriorityAsc()
            .stream()
            .map(FaqResponse::from)
            .toList();
    }

    // =========================
    // FAQ 자동 생성 연계
    // =========================

    /**
     * Domain을 RAGFlow가 지원하는 dataset 값으로 매핑
     * 
     * 현재 RAGFlow는 'POLICY', 'TEST'만 지원하므로,
     * 모든 domain을 'POLICY'로 매핑합니다.
     * 
     * @param domain 원본 domain (예: "HR", "SECURITY", "POLICY" 등)
     * @return RAGFlow가 지원하는 dataset 값
     */
    private String mapDomainToRagflowDataset(String domain) {
        // RAGFlow가 지원하는 dataset: 'POLICY', 'TEST'
        // 모든 domain을 'POLICY'로 매핑 (필요시 확장 가능)
        if (domain == null || domain.isBlank()) {
            return "POLICY";
        }
        
        // 대소문자 무시하고 매핑
        String upperDomain = domain.toUpperCase();
        
        // 이미 RAGFlow가 지원하는 값이면 그대로 사용
        if ("POLICY".equals(upperDomain) || "TEST".equals(upperDomain)) {
            return upperDomain;
        }
        
        // 그 외의 모든 domain은 'POLICY'로 매핑
        return "POLICY";
    }

    @Override
    public UUID generateDraftFromCandidate(UUID candidateId) {
        FaqCandidate candidate = faqCandidateRepository.findById(candidateId)
            .orElseThrow(() -> new IllegalArgumentException("FAQ 후보가 존재하지 않습니다."));

        // PII / 의도 신뢰도 정책
        if (Boolean.TRUE.equals(candidate.getPiiDetected())) {
            candidate.setStatus(FaqCandidate.CandidateStatus.EXCLUDED);
            throw new IllegalArgumentException("PII가 감지된 FAQ 후보는 Draft를 생성할 수 없습니다.");
        }

        if (candidate.getAvgIntentConfidence() == null || candidate.getAvgIntentConfidence() < 0.7) {
            candidate.setStatus(FaqCandidate.CandidateStatus.EXCLUDED);
            throw new IllegalArgumentException(
                String.format("의도 신뢰도가 부족합니다. (현재: %s, 최소 요구: 0.7)", 
                    candidate.getAvgIntentConfidence())
            );
        }

        // ======================================
        // 🔹 RAG 검색 연동 (LLM 미사용)
        // ======================================
        List<AiSearchResponse.Result> searchResults =
            searchFacade.searchDocs(
                candidate.getCanonicalQuestion(),
                SearchDataset.POLICY,
                5
            );

        // RAG 검색 결과를 AI 서비스 요청 DTO로 변환
        List<FaqAiClient.TopDoc> topDocs = searchResults.stream()
            .map(result -> new FaqAiClient.TopDoc(
                result.getDocId(),
                result.getTitle(),
                result.getSnippet(),
                result.getScore(),
                result.getPage(),
                result.getDataset(),
                result.getSource()
            ))
            .toList();

        // ======================================
        // AI 서비스 호출 (RAG + LLM을 사용한 FAQ 초안 생성)
        // ======================================
        // ⚠️ AI 서비스가 domain을 RAGFlow dataset으로 사용하므로,
        // RAGFlow가 지원하는 값으로 매핑 (POLICY, TEST 등)
        // 현재 RAGFlow는 'POLICY', 'TEST'만 지원하므로, 모든 domain을 'POLICY'로 매핑
        String mappedDomain = mapDomainToRagflowDataset(candidate.getDomain());
        
        FaqAiClient.AiFaqResponse aiResponse =
            faqAiClient.generate(
                mappedDomain,  // RAGFlow가 지원하는 dataset 값으로 매핑
                candidate.getId().toString(), // cluster_id 대체
                candidate.getCanonicalQuestion(),
                topDocs  // RAG 검색 결과 전달
            );

        // AI 서비스 응답 검증
        if (!"SUCCESS".equals(aiResponse.status()) || aiResponse.faq_draft() == null) {
            String errorMsg = aiResponse.error_message() != null 
                ? aiResponse.error_message() 
                : "AI 서비스에서 FAQ 초안 생성에 실패했습니다.";
            throw new IllegalStateException(
                String.format("FAQ 초안 생성 실패: %s (status: %s)", errorMsg, aiResponse.status())
            );
        }

        FaqDraft draft = FaqDraft.builder()
            .faqDraftId(aiResponse.faq_draft().faq_draft_id())
            .domain(candidate.getDomain())
            .clusterId(candidate.getId().toString())
            .question(aiResponse.faq_draft().question())
            .answerMarkdown(aiResponse.faq_draft().answer_markdown())
            .summary(aiResponse.faq_draft().summary())
            .aiConfidence(aiResponse.faq_draft().ai_confidence())
            .status(FaqDraft.Status.DRAFT)
            .createdAt(java.time.LocalDateTime.now())
            .build();

        faqDraftRepository.save(draft);
        return draft.getId();
    }

    @Override
    public void approveDraft(UUID draftId, UUID reviewerId, String question, String answer) {
        FaqDraft draft = faqDraftRepository.findById(draftId)
            .orElseThrow(() -> new IllegalArgumentException("FAQ 초안이 존재하지 않습니다."));

        // 이미 승인된 Draft는 다시 승인할 수 없음
        if (draft.getStatus() == FaqDraft.Status.PUBLISHED) {
            throw new IllegalStateException("이미 승인된 FAQ 초안입니다.");
        }

        // 이미 반려된 Draft는 승인할 수 없음
        if (draft.getStatus() == FaqDraft.Status.REJECTED) {
            throw new IllegalStateException("반려된 FAQ 초안은 승인할 수 없습니다.");
        }

        // 게시 FAQ 생성
        Faq faq = new Faq();
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setDomain(draft.getDomain());
        faq.setIsActive(true);
        faq.setCreatedAt(Instant.now());
        faq.setUpdatedAt(Instant.now());

        faqRepository.save(faq);

        // 초안 상태 변경
        draft.publish(reviewerId);

        // 관리자 이력
        FaqRevision revision = FaqRevision.create(
            "FAQ_DRAFT",
            draft.getId(),
            "APPROVE",
            reviewerId,
            null
        );

        faqRevisionRepository.save(revision);
    }

    @Override
    public void rejectDraft(UUID draftId, UUID reviewerId, String reason) {
        FaqDraft draft = faqDraftRepository.findById(draftId)
            .orElseThrow(() -> new IllegalArgumentException("FAQ 초안이 존재하지 않습니다."));

        // 이미 승인된 Draft는 반려할 수 없음
        if (draft.getStatus() == FaqDraft.Status.PUBLISHED) {
            throw new IllegalStateException("이미 승인된 FAQ 초안은 반려할 수 없습니다.");
        }

        // 이미 반려된 Draft는 다시 반려할 수 없음
        if (draft.getStatus() == FaqDraft.Status.REJECTED) {
            throw new IllegalStateException("이미 반려된 FAQ 초안입니다.");
        }

        draft.reject(reviewerId);

        FaqRevision revision = FaqRevision.create(
            "FAQ_DRAFT",
            draft.getId(),
            "REJECT",
            reviewerId,
            reason
        );

        faqRevisionRepository.save(revision);
    }
}
