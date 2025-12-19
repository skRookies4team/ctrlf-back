package com.ctrlf.education.quiz.service;

import static com.ctrlf.education.quiz.dto.QuizRequest.*;
import static com.ctrlf.education.quiz.dto.QuizResponse.*;

import com.ctrlf.education.entity.Education;
import com.ctrlf.education.quiz.entity.QuizAttempt;
import com.ctrlf.education.quiz.entity.QuizLeaveTracking;
import com.ctrlf.education.quiz.entity.QuizQuestion;
import com.ctrlf.education.quiz.client.QuizAiClient;
import com.ctrlf.education.quiz.client.QuizAiDtos;
import com.ctrlf.education.quiz.repository.QuizAttemptRepository;
import com.ctrlf.education.quiz.repository.QuizLeaveTrackingRepository;
import com.ctrlf.education.quiz.repository.QuizQuestionRepository;
import com.ctrlf.education.repository.EducationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizAttemptRepository attemptRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizLeaveTrackingRepository leaveRepository;
    private final EducationRepository educationRepository;
    private final ObjectMapper objectMapper;
    private final QuizAiClient quizAiClient;

    @Transactional
    public StartResponse start(UUID educationId, UUID userUuid) {
        Education edu = educationRepository.findById(educationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found"));
        // restore existing attempt if not submitted
        Optional<QuizAttempt> existing = attemptRepository.findTopByUserUuidAndEducationIdAndSubmittedAtIsNullOrderByCreatedAtDesc(userUuid, educationId);
        QuizAttempt attempt;
        if (existing.isPresent()) {
            attempt = existing.get();
        } else {
            attempt = new QuizAttempt();
            attempt.setUserUuid(userUuid);
            attempt.setEducationId(educationId);
            long cnt = attemptRepository.countByUserUuidAndEducationId(userUuid, educationId);
            attempt.setAttemptNo((int) cnt + 1);
            attempt = attemptRepository.save(attempt);
            // AI 서버로 문항 생성 요청 (실패 시 placeholder로 폴백)
            try {
                QuizAiDtos.GenerateRequest req = new QuizAiDtos.GenerateRequest();
                req.setEducationId(educationId.toString());
                req.setAttemptNo(attempt.getAttemptNo());
                req.setLanguage("ko");
                req.setNumQuestions(5);
                req.setQuestionType("MCQ_SINGLE");
                QuizAiDtos.GenerateResponse aiRes = quizAiClient.generate(req);
                List<QuizQuestion> qs = new ArrayList<>();
                if (aiRes != null && aiRes.getQuestions() != null) {
                    for (QuizAiDtos.AiQuestion aq : aiRes.getQuestions()) {
                        QuizQuestion q = new QuizQuestion();
                        q.setAttemptId(attempt.getId());
                        q.setQuestion(aq.getStem());
                        // map options
                        List<String> choices = new ArrayList<>();
                        Integer correctIdx = null;
                        if (aq.getOptions() != null) {
                            int idx = 0;
                            for (QuizAiDtos.AiOption opt : aq.getOptions()) {
                                choices.add(opt.getText());
                                if (Boolean.TRUE.equals(opt.getIsCorrect()) && correctIdx == null) {
                                    correctIdx = idx;
                                }
                                idx++;
                            }
                        }
                        q.setOptions(toJson(choices));
                        q.setCorrectOptionIdx(correctIdx);
                        q.setExplanation(aq.getExplanation());
                        qs.add(q);
                    }
                }
                if (qs.isEmpty()) {
                    qs = generatePlaceholders(attempt.getId());
                }
                questionRepository.saveAll(qs);
            } catch (Exception ex) {
                // 폴백
                List<QuizQuestion> qs = generatePlaceholders(attempt.getId());
                questionRepository.saveAll(qs);
            }
        }
        List<QuizQuestion> list = questionRepository.findByAttemptId(attempt.getId());
        List<QuestionItem> items = new ArrayList<>();
        for (QuizQuestion q : list) {
            items.add(new QuestionItem(
                q.getId(),
                q.getQuestion(),
                parseChoices(q.getOptions()),
                q.getUserSelectedOptionIdx() // null if not submitted
            ));
        }
        return new StartResponse(attempt.getId(), items);
    }

    @Transactional
    public SubmitResponse submit(UUID attemptId, UUID userUuid, SubmitRequest req) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "attempt not found"));
        if (!attempt.getUserUuid().equals(userUuid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        if (attempt.getSubmittedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already submitted");
        }
        List<QuizQuestion> qs = questionRepository.findByAttemptId(attemptId);
        // apply answers
        int correct = 0;
        for (AnswerItem a : req.getAnswers()) {
            QuizQuestion q = qs.stream().filter(x -> x.getId().equals(a.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "question not in attempt"));
            q.setUserSelectedOptionIdx(a.getUserSelectedIndex());
        }
        questionRepository.saveAll(qs);
        // grade
        for (QuizQuestion q : qs) {
            if (q.getUserSelectedOptionIdx() != null
                && q.getCorrectOptionIdx() != null
                && q.getUserSelectedOptionIdx().intValue() == q.getCorrectOptionIdx().intValue()) {
                correct++;
            }
        }
        int total = qs.size();
        int wrong = total - correct;
        int score = total == 0 ? 0 : Math.round(correct * 100f / total);
        // pass criteria
        Education edu = educationRepository.findById(attempt.getEducationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found"));
        boolean passed = edu.getPassScore() != null ? score >= edu.getPassScore() : correct == total;
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setSubmittedAt(Instant.now());
        attemptRepository.save(attempt);
        return new SubmitResponse(score, passed, correct, wrong, total, attempt.getSubmittedAt());
    }

    @Transactional(readOnly = true)
    public ResultResponse result(UUID attemptId, UUID userUuid) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "attempt not found"));
        if (!attempt.getUserUuid().equals(userUuid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        if (attempt.getSubmittedAt() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "attempt not submitted");
        }
        List<QuizQuestion> qs = questionRepository.findByAttemptId(attemptId);
        int correct = 0;
        for (QuizQuestion q : qs) {
            if (q.getUserSelectedOptionIdx() != null
                && q.getCorrectOptionIdx() != null
                && q.getUserSelectedOptionIdx().intValue() == q.getCorrectOptionIdx().intValue()) {
                correct++;
            }
        }
        int total = qs.size();
        int wrong = total - correct;
        return new ResultResponse(
            attempt.getScore() != null ? attempt.getScore() : 0,
            attempt.getPassed() != null ? attempt.getPassed() : false,
            correct, wrong, total,
            attempt.getSubmittedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<WrongNoteItem> wrongs(UUID attemptId, UUID userUuid) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "attempt not found"));
        if (!attempt.getUserUuid().equals(userUuid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        if (attempt.getSubmittedAt() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "attempt not submitted");
        }
        List<WrongNoteItem> items = new ArrayList<>();
        for (QuizQuestion q : questionRepository.findByAttemptId(attemptId)) {
            Integer sel = q.getUserSelectedOptionIdx();
            Integer cor = q.getCorrectOptionIdx();
            if (sel != null && cor != null && !sel.equals(cor)) {
                items.add(new WrongNoteItem(
                    q.getQuestion(),
                    sel,
                    cor,
                    q.getExplanation(),
                    parseChoices(q.getOptions())
                ));
            }
        }
        return items;
    }

    @Transactional
    public LeaveResponse leave(UUID attemptId, UUID userUuid, LeaveRequest req) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "attempt not found"));
        if (!attempt.getUserUuid().equals(userUuid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        if (attempt.getSubmittedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "attempt already submitted");
        }
        QuizLeaveTracking t = leaveRepository.findByAttemptId(attemptId).orElseGet(() -> {
            QuizLeaveTracking nt = new QuizLeaveTracking();
            nt.setAttemptId(attemptId);
            nt.setLeaveCount(0);
            nt.setTotalLeaveSeconds(0);
            return nt;
        });
        t.setLeaveCount(t.getLeaveCount() == null ? 1 : t.getLeaveCount() + 1);
        t.setLastLeaveAt(req.getTimestamp() != null ? req.getTimestamp() : Instant.now());
        leaveRepository.save(t);
        return new LeaveResponse(true, t.getLeaveCount(), t.getLastLeaveAt());
    }

    // Helpers
    private List<String> parseChoices(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<QuizQuestion> generatePlaceholders(UUID attemptId) {
        List<QuizQuestion> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            QuizQuestion q = new QuizQuestion();
            q.setAttemptId(attemptId);
            q.setQuestion("샘플 문제 " + (i + 1));
            q.setOptions(toJson(List.of("보기1", "보기2", "보기3", "보기4", "보기5")));
            q.setCorrectOptionIdx(0);
            q.setExplanation("샘플 해설");
            list.add(q);
        }
        return list;
    }

    private String toJson(List<String> choices) {
        try {
            return objectMapper.writeValueAsString(choices);
        } catch (Exception e) {
            return "[]";
        }
    }
}

