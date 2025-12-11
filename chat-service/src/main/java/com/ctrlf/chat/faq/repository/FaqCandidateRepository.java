package com.ctrlf.chat.faq.repository;

import com.ctrlf.chat.faq.entity.FaqCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FaqCandidateRepository extends JpaRepository<FaqCandidate, UUID> {

    // 🔥 활성 후보만 가져오고 생성일 내림차순 정렬
    List<FaqCandidate> findAllByIsDisabledFalseOrderByCreatedAtDesc();
}
