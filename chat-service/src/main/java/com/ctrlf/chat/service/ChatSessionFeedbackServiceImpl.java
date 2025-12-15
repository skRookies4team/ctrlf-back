package com.ctrlf.chat.service;

import com.ctrlf.chat.entity.ChatSessionFeedback;
import com.ctrlf.chat.repository.ChatSessionFeedbackRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionFeedbackServiceImpl implements ChatSessionFeedbackService {

    private final ChatSessionFeedbackRepository chatSessionFeedbackRepository;

    @Override
    public void submitSessionFeedback(
        UUID sessionId,
        UUID userUuid,
        Integer score,
        String comment
    ) {
        ChatSessionFeedback feedback =
            chatSessionFeedbackRepository.findBySessionId(sessionId)
                .orElseGet(ChatSessionFeedback::new);

        feedback.setSessionId(sessionId);
        feedback.setUserUuid(userUuid);
        feedback.setScore(score);
        feedback.setComment(comment);
        feedback.setCreatedAt(Instant.now());

        chatSessionFeedbackRepository.save(feedback);
    }
}
