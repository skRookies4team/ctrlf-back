package com.ctrlf.chat.faq.service;

import com.ctrlf.chat.faq.dto.request.AutoFaqGenerateRequest;
import com.ctrlf.chat.faq.dto.request.FaqCreateRequest;
import com.ctrlf.chat.faq.dto.request.FaqDraftGenerateBatchRequest;
import com.ctrlf.chat.faq.dto.request.FaqUpdateRequest;
import com.ctrlf.chat.faq.dto.response.AutoFaqGenerateResponse;
import com.ctrlf.chat.faq.dto.response.FaqDraftGenerateBatchResponse;
import com.ctrlf.chat.faq.dto.response.FaqResponse;

import java.util.List;
import java.util.UUID;

public interface FaqService {

    /** (기존) FAQ 수동 생성 */
    UUID create(FaqCreateRequest request);

    /** (기존) FAQ 수정 */
    void update(UUID id, FaqUpdateRequest request);

    /** (기존) FAQ 삭제(비활성) */
    void delete(UUID id);

    /** (기존) 게시 FAQ 조회 */
    List<FaqResponse> getAll();

    // =========================
    // FAQ 자동 생성 연계 (추가)
    // =========================

    /** 자동 FAQ 생성 (질문 로그 기반) */
    AutoFaqGenerateResponse generateAuto(AutoFaqGenerateRequest request);

    /** FAQ 후보 → AI 초안 생성 (기존, 유지) */
    UUID generateDraftFromCandidate(UUID candidateId);

    /** FAQ 초안 배치 생성 (Phase 20-AI-2) */
    FaqDraftGenerateBatchResponse generateDraftBatch(FaqDraftGenerateBatchRequest request);

    /** AI 초안 승인 → FAQ 게시 */
    void approveDraft(UUID draftId, UUID reviewerId, String question, String answer);

    /** AI 초안 반려 */
    void rejectDraft(UUID draftId, UUID reviewerId, String reason);
}
