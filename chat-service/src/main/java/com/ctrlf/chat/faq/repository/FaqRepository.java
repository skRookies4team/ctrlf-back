package com.ctrlf.chat.faq.repository;

import com.ctrlf.chat.faq.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FaqRepository extends JpaRepository<Faq, UUID> {
    List<Faq> findByDomain(String domain);
    List<Faq> findByIsActiveTrueOrderByPriorityAsc();
}
