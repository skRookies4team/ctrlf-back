package com.ctrlf.chat.faq.service;

import com.ctrlf.chat.faq.dto.request.FaqCreateRequest;
import com.ctrlf.chat.faq.dto.request.FaqUpdateRequest;
import com.ctrlf.chat.faq.dto.response.FaqResponse;
import com.ctrlf.chat.faq.dto.response.FaqCandidateResponse;
import com.ctrlf.chat.faq.entity.Faq;
import com.ctrlf.chat.faq.entity.FaqCandidate;
import com.ctrlf.chat.faq.exception.FaqNotFoundException;
import com.ctrlf.chat.faq.repository.FaqRepository;
import com.ctrlf.chat.faq.repository.FaqCandidateRepository;
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

    @Override
    public UUID create(FaqCreateRequest request) {
        Faq faq = new Faq();
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setDomain(request.getDomain());
        faq.setPriority(request.getPriority());
        faq.setIsActive(true);
        faq.setCreatedAt(Instant.now());
        faq.setUpdatedAt(Instant.now());

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

    // ✅ FAQ 후보 목록 조회 추가됨
    @Override
    @Transactional(readOnly = true)
    public List<FaqCandidateResponse> getCandidates() {
        return faqCandidateRepository.findAllByIsDisabledFalseOrderByCreatedAtDesc()
            .stream()
            .map(FaqCandidateResponse::from)
            .toList();
    }
}
