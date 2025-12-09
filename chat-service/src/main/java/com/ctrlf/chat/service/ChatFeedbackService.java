package com.ctrlf.chat.service;

import com.ctrlf.chat.entity.ChatFeedback;
import com.ctrlf.chat.repository.ChatFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatFeedbackService {

    private final ChatFeedbackRepository chatFeedbackRepository;

    // ✅ 메시지 피드백
    public void submitMessageFeedback(
        UUID sessionId,
        UUID sectionId,
        UUID messageId,
        UUID userUuid,
        Integer score,
        String comment
    ) {
        ChatFeedback feedback = new ChatFeedback();
        feedback.setSessionId(sessionId);
        feedback.setSectionId(sectionId);
        feedback.setMessageId(messageId);
        feedback.setUserUuid(userUuid);
        feedback.setScore(score);
        feedback.setComment(comment);

        chatFeedbackRepository.save(feedback);
    }

    // ✅ 세션 종료 총평
    public void submitSessionFeedback(
        UUID sessionId,
        UUID userUuid,
        Integer score,
        String comment
    ) {
        ChatFeedback feedback = new ChatFeedback();
        feedback.setSessionId(sessionId);
        feedback.setUserUuid(userUuid);
        feedback.setScore(score);
        feedback.setComment(comment);

        chatFeedbackRepository.save(feedback);
    }
}
