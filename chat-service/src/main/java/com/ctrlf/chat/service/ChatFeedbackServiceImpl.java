package com.ctrlf.chat.service;

import com.ctrlf.chat.entity.ChatFeedback;
import com.ctrlf.chat.repository.ChatFeedbackRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatFeedbackServiceImpl implements ChatFeedbackService {

    private final ChatFeedbackRepository chatFeedbackRepository;

    @Override
    public void submitMessageFeedback(
        UUID sessionId,
        UUID messageId,
        UUID userUuid,
        Integer score,
        String comment
    ) {
        ChatFeedback feedback = new ChatFeedback();
        feedback.setSessionId(sessionId);
        feedback.setMessageId(messageId);
        feedback.setUserUuid(userUuid);
        feedback.setScore(score);
        feedback.setComment(comment);
        feedback.setCreatedAt(Instant.now());

        chatFeedbackRepository.save(feedback);
    }
}
