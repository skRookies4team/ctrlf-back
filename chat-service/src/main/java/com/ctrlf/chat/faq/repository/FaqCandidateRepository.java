package com.ctrlf.chat.faq.repository;

import com.ctrlf.chat.faq.entity.FaqCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FaqCandidateRepository extends JpaRepository<FaqCandidate, UUID> {
}

