package com.ctrlf.education.quiz.service;

import static com.ctrlf.education.quiz.dto.QuizRequest.*;
import static com.ctrlf.education.quiz.dto.QuizResponse.*;
import com.ctrlf.education.quiz.dto.QuizResponse.DepartmentStatsItem;
import com.ctrlf.education.quiz.dto.QuizResponse.RetryInfoResponse;

import com.ctrlf.education.entity.Education;
import com.ctrlf.education.entity.EducationProgress;
import com.ctrlf.education.quiz.entity.QuizAttempt;
import com.ctrlf.education.quiz.entity.QuizLeaveTracking;
import com.ctrlf.education.quiz.entity.QuizQuestion;
import com.ctrlf.education.quiz.client.QuizAiClient;
import com.ctrlf.education.quiz.client.QuizAiDtos;
import com.ctrlf.education.quiz.repository.QuizAttemptRepository;
import com.ctrlf.education.quiz.repository.QuizLeaveTrackingRepository;
import com.ctrlf.education.quiz.repository.QuizQuestionRepository;
import com.ctrlf.education.repository.EducationRepository;
import com.ctrlf.education.repository.EducationProgressRepository;
import com.ctrlf.education.script.entity.EducationScript;
import com.ctrlf.education.script.entity.EducationScriptScene;
import com.ctrlf.education.script.repository.EducationScriptRepository;
import com.ctrlf.education.script.repository.EducationScriptSceneRepository;
import com.ctrlf.education.video.entity.SourceSet;
import com.ctrlf.education.video.entity.SourceSetDocument;
import com.ctrlf.education.video.repository.SourceSetRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 퀴즈 서비스.
 * 퀴즈 시작, 제출, 결과 조회, 오답노트, 이탈 기록, 임시 저장 등의 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final QuizAttemptRepository attemptRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizLeaveTrackingRepository leaveRepository;
    private final EducationRepository educationRepository;
    private final EducationProgressRepository progressRepository;
    private final EducationScriptRepository scriptRepository;
    private final EducationScriptSceneRepository sceneRepository;
    private final SourceSetRepository sourceSetRepository;
    private final ObjectMapper objectMapper;
    private final QuizAiClient quizAiClient;

    /**
     * 퀴즈 시작
     * 
     * <p>새로운 퀴즈 시도를 생성하거나, 기존 미제출 시도를 복원합니다.
     * AI 서버에서 문항을 생성하며, 실패 시 placeholder 문항으로 폴백합니다.
     * 복원 시 저장된 답안을 포함하여 반환합니다.
     * 새 시도 생성 시 시간 제한을 15분(900초)으로 설정합니다
     * 
     * @param educationId 교육 ID
     * @param userUuid 사용자 UUID
     * @return 시도 ID와 문항 목록 (저장된 답안 포함)
     * @throws ResponseStatusException 교육을 찾을 수 없으면 404
     */
    @Transactional
    public StartResponse start(UUID educationId, UUID userUuid) {
        educationRepository.findById(educationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found"));
        
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
            // 시간 제한 설정 (기본값: 15분 = 900초)
            attempt.setTimeLimit(900);
            attempt = attemptRepository.save(attempt);
            // AI 서버로 문항 생성 요청 (실패 시 placeholder로 폴백)
            try {
                // EducationScript 조회 (docId, docVersion) - 최신 버전 우선
                Optional<EducationScript> scriptOpt = scriptRepository
                    .findByEducationIdAndDeletedAtIsNullOrderByVersionDesc(educationId)
                    .stream()
                    .findFirst();
                
                QuizAiDtos.GenerateRequest req = new QuizAiDtos.GenerateRequest();
                req.setLanguage("ko");
                req.setNumQuestions(5);
                req.setMaxOptions(4); // 기본값 4
                
                // 퀴즈 후보 블록 추출 (모든 씬의 텍스트)
                List<QuizAiDtos.QuizCandidateBlock> candidateBlocks = new ArrayList<>();
                String docId = null;
                String docVersion = null;
                
                // SourceSet에서 첫 번째 문서 ID 가져오기 (docId용)
                List<SourceSet> sourceSets = sourceSetRepository.findByEducationIdAndNotDeleted(educationId);
                if (!sourceSets.isEmpty()) {
                    SourceSet sourceSet = sourceSets.get(0);
                    List<SourceSetDocument> documents = sourceSet.getDocuments();
                    if (documents != null && !documents.isEmpty()) {
                        // 첫 번째 문서 ID 사용
                        docId = documents.get(0).getDocumentId().toString();
                    }
                }
                
                if (scriptOpt.isPresent()) {
                    EducationScript script = scriptOpt.get();
                    if (script.getVersion() != null) {
                        docVersion = "v" + script.getVersion();
                    }
                    
                    List<EducationScriptScene> scenes = sceneRepository
                        .findByScriptIdOrderByChapterIdAscSceneIndexAsc(script.getId());
                    
                    for (EducationScriptScene scene : scenes) {
                        if (scene.getDeletedAt() != null) {
                            continue; // 삭제된 씬 제외
                        }
                        
                        // 텍스트 추출: narration 우선, 없으면 caption, 둘 다 없으면 visual
                        String text = scene.getNarration();
                        if (text == null || text.trim().isEmpty()) {
                            text = scene.getCaption();
                        }
                        if (text == null || text.trim().isEmpty()) {
                            text = scene.getVisual();
                        }
                        
                        // 텍스트가 있는 경우만 블록으로 추가
                        if (text != null && !text.trim().isEmpty()) {
                            candidateBlocks.add(new QuizAiDtos.QuizCandidateBlock(
                                scene.getId().toString(),                    // blockId
                                docId,                                       // docId
                                docVersion,                                  // docVersion
                                scene.getChapterId() != null ? scene.getChapterId().toString() : null, // chapterId
                                null,                                        // learningObjectiveId (없음)
                                text.trim(),                                 // text
                                new ArrayList<>(),                           // tags (빈 리스트)
                                null                                         // articlePath (없음)
                            ));
                        }
                    }
                }
                req.setQuizCandidateBlocks(candidateBlocks);
                
                // 재응시 시 이전 문항 제외
                if (attempt.getAttemptNo() > 1) {
                    List<QuizAiDtos.ExcludePreviousQuestion> excludeQuestions = new ArrayList<>();
                    List<QuizAttempt> previousAttempts = attemptRepository
                        .findByUserUuidAndEducationIdAndSubmittedAtIsNotNullOrderByCreatedAtDesc(userUuid, educationId);
                    for (QuizAttempt prevAttempt : previousAttempts) {
                        List<QuizQuestion> prevQuestions = questionRepository.findByAttemptId(prevAttempt.getId());
                        for (QuizQuestion prevQ : prevQuestions) {
                            if (prevQ.getQuestion() != null && prevQ.getId() != null) {
                                // 중복 체크: 이미 같은 questionId가 있는지 확인
                                boolean alreadyExists = excludeQuestions.stream()
                                    .anyMatch(eq -> eq.getQuestionId() != null && eq.getQuestionId().equals(prevQ.getId().toString()));
                                if (!alreadyExists) {
                                    excludeQuestions.add(new QuizAiDtos.ExcludePreviousQuestion(
                                        prevQ.getId().toString(),  // questionId
                                        prevQ.getQuestion()        // stem
                                    ));
                                }
                            }
                        }
                    }
                    req.setExcludePreviousQuestions(excludeQuestions);
                }

                // AI 서버 요청 데이터 로깅
                try {
                    String requestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(req);
                    log.info("AI 서버 퀴즈 생성 요청 (educationId: {}, attemptNo: {}):\n{}", 
                        educationId, attempt.getAttemptNo(), requestJson);
                } catch (Exception e) {
                    log.warn("AI 서버 요청 데이터 로깅 실패: {}", e.getMessage());
                }

                // ai 서버 요청
                QuizAiDtos.GenerateResponse aiRes = quizAiClient.generate(req);
                List<QuizQuestion> qs = new ArrayList<>();
                if (aiRes != null && aiRes.getQuestions() != null) {
                    int order = 0;
                    for (QuizAiDtos.AiQuestion aq : aiRes.getQuestions()) {
                        QuizQuestion q = new QuizQuestion();
                        q.setAttemptId(attempt.getId());
                        q.setQuestion(aq.getStem());
                        q.setQuestionOrder(order); // 문항 순서 설정
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
                        order++;
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
        List<QuizQuestion> list = questionRepository.findByAttemptIdOrderByQuestionOrderAsc(attempt.getId());
        List<QuestionItem> items = new ArrayList<>();
        for (QuizQuestion q : list) {
            items.add(new QuestionItem(
                q.getId(),
                q.getQuestionOrder() != null ? q.getQuestionOrder() : 0, // 순서 (기본값 0)
                q.getQuestion(),
                parseChoices(q.getOptions()),
                q.getUserSelectedOptionIdx() // null if not submitted
            ));
        }
        return new StartResponse(attempt.getId(), items);
    }

    /**
     * 응답 임시 저장 (FS-QUIZ-PLAY-03).
     * 
     * <p>진행 중인 답안을 임시 저장하여 페이지 새로고침/이탈 시 복구할 수 있도록 합니다.
     * 저장된 답안은 {@link #start(UUID, UUID)} 호출 시 자동으로 복원됩니다.
     * 
     * @param attemptId 시도 ID
     * @param userUuid 사용자 UUID
     * @param req 저장할 답안 목록
     * @return 저장 성공 여부, 저장된 답안 개수, 저장 시각
     * @throws ResponseStatusException 시도를 찾을 수 없으면 404, 권한 없으면 403, 이미 제출했으면 409
     */
    @Transactional
    public SaveResponse save(UUID attemptId, UUID userUuid, SaveRequest req) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "attempt not found"));
        if (!attempt.getUserUuid().equals(userUuid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        if (attempt.getSubmittedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "attempt already submitted");
        }
        List<QuizQuestion> qs = questionRepository.findByAttemptId(attemptId);
        // apply answers (임시 저장)
        int savedCount = 0;
        for (AnswerItem a : req.getAnswers()) {
            QuizQuestion q = qs.stream().filter(x -> x.getId().equals(a.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "question not in attempt"));
            q.setUserSelectedOptionIdx(a.getUserSelectedIndex());
            savedCount++;
        }
        questionRepository.saveAll(qs);
        return new SaveResponse(true, savedCount, Instant.now());
    }

    /**
     * 퀴즈 제출 및 채점 (FS-QUIZ-RESULT-01).
     * 
     * <p>사용자가 선택한 답안을 저장하고 채점을 수행합니다.
     * 정답 개수를 계산하여 점수를 산출하고, 교육의 통과 기준 점수와 비교하여 합격 여부를 결정합니다.
     * 제출 시각을 기록하여 이후 재제출을 방지합니다.
     * 
     * @param attemptId 시도 ID
     * @param userUuid 사용자 UUID
     * @param req 제출할 답안 목록
     * @return 점수, 통과 여부, 정답/오답 개수, 제출 시각
     * @throws ResponseStatusException 시도를 찾을 수 없으면 404, 권한 없으면 403, 이미 제출했으면 409
     */
    @Transactional
    public SubmitResponse submit(UUID attemptId, UUID userUuid, String department, SubmitRequest req) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "attempt not found"));
        if (!attempt.getUserUuid().equals(userUuid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        if (attempt.getSubmittedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already submitted");
        }
        List<QuizQuestion> qs = questionRepository.findByAttemptId(attemptId);
        int total = qs.size();
        
        // 제출한 답안만 업데이트 및 채점
        int correct = 0;
        for (AnswerItem a : req.getAnswers()) {
            QuizQuestion q = qs.stream().filter(x -> x.getId().equals(a.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "question not in attempt"));
            q.setUserSelectedOptionIdx(a.getUserSelectedIndex());
            
            // 제출한 답안 채점
            if (q.getCorrectOptionIdx() != null
                && a.getUserSelectedIndex() != null
                && a.getUserSelectedIndex().intValue() == q.getCorrectOptionIdx().intValue()) {
                correct++;
            }
        }
        questionRepository.saveAll(qs);
        
        // 제출하지 않은 문항은 오답 처리 (userSelectedOptionIdx가 null이므로 자동으로 오답)
        int wrong = total - correct;
        int score = total == 0 ? 0 : Math.round(correct * 100f / total);
        // pass criteria
        Education edu = educationRepository.findById(attempt.getEducationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found"));
        boolean passed = edu.getPassScore() != null ? score >= edu.getPassScore() : correct == total;
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setSubmittedAt(Instant.now());
        // 부서 정보 저장 (부서별 통계용)
        if (department != null && !department.isBlank()) {
            attempt.setDepartment(department);
        }
        attemptRepository.save(attempt);
        return new SubmitResponse(score, passed, correct, wrong, total, attempt.getSubmittedAt());
    }

    /**
     * 퀴즈 결과 조회 (FS-QUIZ-RESULT-02).
     * 
     * <p>제출 완료된 퀴즈의 결과를 조회합니다.
     * 점수, 통과 여부, 정답/오답 개수를 반환합니다.
     * 
     * @param attemptId 시도 ID
     * @param userUuid 사용자 UUID
     * @return 점수, 통과 여부, 정답/오답 개수, 완료 시각
     * @throws ResponseStatusException 시도를 찾을 수 없으면 404, 권한 없으면 403, 아직 제출하지 않았으면 409
     */
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

    /**
     * 오답노트 목록 조회 (FS-QUIZ-REVIEW-02).
     * 
     * <p>제출 완료된 퀴즈에서 틀린 문항만 조회합니다.
     * 정답 번호는 노출하지 않고 해설 텍스트만 제공합니다.
     * 
     * @param attemptId 시도 ID
     * @param userUuid 사용자 UUID
     * @return 틀린 문항 목록 (질문, 사용자 선택, 정답 인덱스, 해설, 보기)
     * @throws ResponseStatusException 시도를 찾을 수 없으면 404, 권한 없으면 403, 아직 제출하지 않았으면 409
     */
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

    /**
     * 퀴즈 이탈 기록 (FS-QUIZ-PLAY-06).
     * 
     * <p>퀴즈 풀이 중 탭/창 전환 등 이탈 이벤트를 기록합니다.
     * 이탈 횟수, 이탈 누적 시간(초), 마지막 이탈 시각을 저장하여 관리자 통계에 활용합니다.
     * 
     * @param attemptId 시도 ID
     * @param userUuid 사용자 UUID
     * @param req 이탈 시각, 사유, 이탈 시간(초)
     * @return 기록 성공 여부, 누적 이탈 횟수, 마지막 이탈 시각
     * @throws ResponseStatusException 시도를 찾을 수 없으면 404, 권한 없으면 403, 이미 제출했으면 409
     */
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
        // 이탈 시간 누적
        if (req.getLeaveSeconds() != null && req.getLeaveSeconds() > 0) {
            t.setTotalLeaveSeconds((t.getTotalLeaveSeconds() == null ? 0 : t.getTotalLeaveSeconds()) + req.getLeaveSeconds());
        }
        leaveRepository.save(t);
        return new LeaveResponse(true, t.getLeaveCount(), t.getLastLeaveAt());
    }

    /**
     * 타이머 정보 조회 (FS-QUIZ-PLAY-02).
     * 
     * <p>퀴즈 풀이 중 시간 제한, 남은 시간, 만료 여부를 조회합니다.
     * 클라이언트 타이머 동기화 및 시간 만료 시 자동 제출에 사용됩니다.
     * 
     * @param attemptId 시도 ID
     * @param userUuid 사용자 UUID
     * @return 시간 제한, 시작 시각, 만료 시각, 남은 시간(초), 만료 여부
     * @throws ResponseStatusException 시도를 찾을 수 없으면 404, 권한 없으면 403, 이미 제출했으면 409
     */
    @Transactional(readOnly = true)
    public TimerResponse getTimer(UUID attemptId, UUID userUuid) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "attempt not found"));
        if (!attempt.getUserUuid().equals(userUuid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        }
        if (attempt.getSubmittedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "attempt already submitted");
        }
        
        Integer timeLimit = attempt.getTimeLimit();
        Instant startedAt = attempt.getCreatedAt();
        Instant now = Instant.now();
        
        Instant expiresAt = null;
        Long remainingSeconds = null;
        Boolean isExpired = false;
        
        if (timeLimit != null && startedAt != null) {
            expiresAt = startedAt.plusSeconds(timeLimit);
            long elapsed = now.getEpochSecond() - startedAt.getEpochSecond();
            remainingSeconds = Math.max(0, timeLimit - elapsed);
            isExpired = remainingSeconds <= 0;
        }
        
        return new TimerResponse(
            timeLimit,
            startedAt,
            expiresAt,
            remainingSeconds,
            isExpired
        );
    }

    /**
     * 풀 수 있는 퀴즈 목록 조회 (FS-QUIZ-START-01).
     * 
     * <p>이수 완료한 교육만 리스트에 노출하며, 풀었던 것과 풀지 않은 것을 구분하여 표시합니다.
     * 각 교육별로 응시 횟수, 최고 점수, 통과 여부를 포함합니다.
     * 
     * @param userUuid 사용자 UUID
     * @return 이수 완료한 교육 목록 (응시 여부, 최고 점수, 통과 여부 포함)
     */
    @Transactional(readOnly = true)
    public List<AvailableEducationItem> getAvailableEducations(UUID userUuid) {
        // 이수 완료한 교육 목록 조회
        List<EducationProgress> completedProgresses = progressRepository.findByUserUuidAndIsCompletedTrue(userUuid);
        List<UUID> completedEducationIds = completedProgresses.stream()
            .map(EducationProgress::getEducationId)
            .filter(java.util.Objects::nonNull) // null 제외
            .distinct() // 중복 제거
            .toList();
        
        if (completedEducationIds.isEmpty()) {
            return List.of();
        }
        
        // 교육 정보 조회
        List<Education> educations = educationRepository.findAllById(completedEducationIds);
        
        // 사용자의 퀴즈 응시 이력 조회 (교육별 최고 점수 포함)
        List<QuizAttempt> allAttempts = attemptRepository.findByUserUuidAndSubmittedAtIsNotNullOrderByCreatedAtDesc(userUuid);
        Map<UUID, QuizAttempt> bestAttemptByEducation = new HashMap<>();
        Map<UUID, Integer> attemptCountByEducation = new HashMap<>();
        
        for (QuizAttempt attempt : allAttempts) {
            UUID eduId = attempt.getEducationId();
            if (eduId == null || !completedEducationIds.contains(eduId)) {
                continue; // educationId가 null이거나 이수 완료한 교육만 대상
            }
            
            // 응시 횟수 카운트
            attemptCountByEducation.merge(eduId, 1, Integer::sum);
            
            // 최고 점수 시도 저장
            QuizAttempt existing = bestAttemptByEducation.get(eduId);
            if (existing == null || 
                (attempt.getScore() != null && existing.getScore() != null && 
                 attempt.getScore() > existing.getScore())) {
                bestAttemptByEducation.put(eduId, attempt);
            }
        }
        
        List<AvailableEducationItem> items = new ArrayList<>();
        
        for (Education edu : educations) {
            if (edu == null || edu.getId() == null || edu.getDeletedAt() != null) {
                continue; // null이거나 삭제된 교육 제외
            }
            
            UUID eduId = edu.getId();
            boolean hasAttempted = bestAttemptByEducation.containsKey(eduId);
            QuizAttempt bestAttempt = bestAttemptByEducation.get(eduId);
            Integer attemptCount = attemptCountByEducation.getOrDefault(eduId, 0);
            
            items.add(new AvailableEducationItem(
                eduId,
                edu.getTitle() != null ? edu.getTitle() : "", // null 방지
                edu.getCategory() != null ? edu.getCategory().name() : null,
                edu.getEduType() != null ? edu.getEduType().name() : null,
                attemptCount,
                null, // 최대 응시 횟수는 추후 정책 테이블에서 조회 가능
                hasAttempted, // 이미 응시했는지 여부
                bestAttempt != null && bestAttempt.getScore() != null ? bestAttempt.getScore() : null, // 최고 점수
                bestAttempt != null && bestAttempt.getPassed() != null ? bestAttempt.getPassed() : null // 통과 여부
            ));
        }
        
        return items;
    }

    /**
     * 내가 풀었던 퀴즈 응시 내역 조회 (FS-MYPAGE-03).
     * 
     * <p>제출 완료된 모든 퀴즈 시도를 조회합니다.
     * 교육별 최고 점수를 계산하여 최고 점수 여부를 표시합니다.
     * 
     * @param userUuid 사용자 UUID
     * @return 응시 내역 목록 (점수, 통과 여부, 시도 회차, 최고 점수 여부 포함)
     */
    @Transactional(readOnly = true)
    public List<MyAttemptItem> getMyAttempts(UUID userUuid) {
        // 제출 완료된 모든 시도 조회
        List<QuizAttempt> attempts = attemptRepository.findByUserUuidAndSubmittedAtIsNotNullOrderByCreatedAtDesc(userUuid);
        
        if (attempts.isEmpty()) {
            return List.of();
        }
        
        // 교육별 최고 점수 계산
        Map<UUID, Integer> bestScoresByEducation = new HashMap<>();
        for (QuizAttempt attempt : attempts) {
            UUID eduId = attempt.getEducationId();
            if (eduId == null) {
                continue; // educationId가 null이면 제외
            }
            Integer score = attempt.getScore() != null ? attempt.getScore() : 0;
            bestScoresByEducation.merge(eduId, score, Math::max);
        }
        
        // 교육 정보 조회 (한 번에)
        Set<UUID> educationIds = attempts.stream()
            .map(QuizAttempt::getEducationId)
            .filter(java.util.Objects::nonNull) // null 제외
            .collect(java.util.stream.Collectors.toSet());
        
        if (educationIds.isEmpty()) {
            return List.of();
        }
        
        Map<UUID, Education> educationMap = educationRepository.findAllById(educationIds).stream()
            .collect(java.util.stream.Collectors.toMap(Education::getId, e -> e));
        
        // 응답 DTO 생성
        List<MyAttemptItem> items = new ArrayList<>();
        for (QuizAttempt attempt : attempts) {
            UUID eduId = attempt.getEducationId();
            if (eduId == null) {
                continue; // educationId가 null이면 제외
            }
            Education edu = educationMap.get(eduId);
            if (edu == null || edu.getDeletedAt() != null) {
                continue; // 삭제된 교육 제외
            }
            Integer score = attempt.getScore() != null ? attempt.getScore() : 0;
            Integer bestScore = bestScoresByEducation.get(eduId);
            boolean isBest = bestScore != null && score.equals(bestScore);
            
            items.add(new MyAttemptItem(
                attempt.getId(),
                eduId,
                edu.getTitle() != null ? edu.getTitle() : "", // null 방지
                score,
                attempt.getPassed(),
                attempt.getAttemptNo(),
                attempt.getSubmittedAt(),
                isBest
            ));
        }
        
        return items;
    }

    /**
     * 부서별 퀴즈 통계 조회.
     * 특정 교육에 대한 부서별 평균 점수와 진행률을 계산합니다.
     *
     * @param educationId 교육 ID (null이면 전체 교육 대상)
     * @return 부서별 통계 목록
     */
    public List<DepartmentStatsItem> getDepartmentStats(UUID educationId) {
        // 교육별 모든 제출 완료된 퀴즈 시도 조회
        List<QuizAttempt> allAttempts;
        if (educationId != null) {
            allAttempts = attemptRepository.findByEducationIdAndSubmittedAtIsNotNull(educationId);
        } else {
            // 전체 교육 대상인 경우 모든 제출된 시도 조회
            allAttempts = attemptRepository.findAll().stream()
                .filter(a -> a.getSubmittedAt() != null && a.getDeletedAt() == null)
                .toList();
        }

        if (allAttempts.isEmpty()) {
            return List.of();
        }

        // 사용자별 최고 점수 계산 (같은 교육 내에서)
        Map<UUID, Integer> bestScoresByUser = new HashMap<>();
        Map<UUID, UUID> educationIdByUser = new HashMap<>();
        
        for (QuizAttempt attempt : allAttempts) {
            UUID userId = attempt.getUserUuid();
            UUID eduId = attempt.getEducationId();
            Integer score = attempt.getScore() != null ? attempt.getScore() : 0;
            
            educationIdByUser.put(userId, eduId);
            bestScoresByUser.merge(userId, score, Math::max);
        }

        // QuizAttempt에 저장된 부서 정보 사용
        Map<UUID, String> userDepartmentMap = new HashMap<>();
        for (QuizAttempt attempt : allAttempts) {
            UUID userId = attempt.getUserUuid();
            String dept = attempt.getDepartment();
            if (dept != null && !dept.isBlank()) {
                // 같은 사용자의 여러 시도 중 부서 정보가 있는 것을 사용
                userDepartmentMap.putIfAbsent(userId, dept);
            }
        }
        // 부서 정보가 없는 사용자는 "기타"로 처리
        for (UUID userId : bestScoresByUser.keySet()) {
            userDepartmentMap.putIfAbsent(userId, "기타");
        }

        // 부서별로 그룹화하여 통계 계산
        Map<String, List<Integer>> scoresByDepartment = new HashMap<>();
        Map<String, Integer> participantCountByDepartment = new HashMap<>();
        
        for (Map.Entry<UUID, Integer> entry : bestScoresByUser.entrySet()) {
            UUID userId = entry.getKey();
            Integer score = entry.getValue();
            String department = userDepartmentMap.getOrDefault(userId, "기타");
            
            scoresByDepartment.computeIfAbsent(department, k -> new ArrayList<>()).add(score);
            participantCountByDepartment.merge(department, 1, Integer::sum);
        }

        // 부서별 평균 점수 및 진행률 계산
        List<DepartmentStatsItem> stats = new ArrayList<>();
        int totalParticipants = bestScoresByUser.size();
        
        for (Map.Entry<String, List<Integer>> entry : scoresByDepartment.entrySet()) {
            String department = entry.getKey();
            List<Integer> scores = entry.getValue();
            int participantCount = participantCountByDepartment.get(department);
            
            // 평균 점수 계산
            double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            
            // 진행률 계산 (해당 부서 참여자 수 / 전체 참여자 수 * 100)
            // 또는 교육 이수 완료율로 계산할 수도 있음
            int progressPercent = totalParticipants > 0 
                ? (int) Math.round((participantCount * 100.0) / totalParticipants)
                : 0;
            
            stats.add(new DepartmentStatsItem(
                department,
                (int) Math.round(avgScore),
                progressPercent,
                participantCount
            ));
        }

        // 평균 점수 내림차순으로 정렬
        stats.sort((a, b) -> Integer.compare(b.getAverageScore(), a.getAverageScore()));
        
        return stats;
    }

    /**
     * 퀴즈 재응시 정보 조회.
     * 특정 교육에 대한 재응시 가능 여부 및 관련 정보를 반환합니다.
     *
     * @param educationId 교육 ID
     * @param userUuid 사용자 UUID
     * @return 재응시 정보 (가능 여부, 응시 횟수, 최고 점수 등)
     * @throws ResponseStatusException 교육을 찾을 수 없으면 404
     */
    @Transactional(readOnly = true)
    public RetryInfoResponse getRetryInfo(UUID educationId, UUID userUuid) {
        Education education = educationRepository.findById(educationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "education not found"));

        // 사용자의 해당 교육 퀴즈 응시 이력 조회
        List<QuizAttempt> attempts = attemptRepository
            .findByUserUuidAndEducationIdAndSubmittedAtIsNotNullOrderByCreatedAtDesc(userUuid, educationId);
        
        int currentAttemptCount = attempts.size();
        
        // 최고 점수 및 통과 여부 계산
        Integer bestScore = null;
        Boolean passed = null;
        Instant lastAttemptAt = null;
        
        if (!attempts.isEmpty()) {
            QuizAttempt bestAttempt = attempts.stream()
                .filter(a -> a.getScore() != null)
                .max((a1, a2) -> Integer.compare(
                    a1.getScore() != null ? a1.getScore() : 0,
                    a2.getScore() != null ? a2.getScore() : 0
                ))
                .orElse(null);
            
            if (bestAttempt != null) {
                bestScore = bestAttempt.getScore();
                passed = bestAttempt.getPassed();
            }
            
            // 마지막 응시 시각
            QuizAttempt lastAttempt = attempts.get(0);
            lastAttemptAt = lastAttempt.getSubmittedAt();
        }
        
        // 최대 응시 횟수 (현재는 null = 무제한, 추후 정책 테이블에서 조회 가능)
        Integer maxAttempts = 2;
        
        // 재응시 가능 여부 판단
        // - maxAttempts가 있으면 currentAttemptCount < maxAttempts일 때 재응시 가능
        boolean canRetry = maxAttempts == null || currentAttemptCount < maxAttempts;
        
        // 남은 응시 횟수 계산
        Integer remainingAttempts = null;
        if (maxAttempts != null) {
            remainingAttempts = Math.max(0, maxAttempts - currentAttemptCount);
        }
        
        return new RetryInfoResponse(
            educationId,
            education.getTitle() != null ? education.getTitle() : "",
            canRetry,
            currentAttemptCount,
            maxAttempts,
            remainingAttempts,
            bestScore,
            passed,
            lastAttemptAt
        );
    }

    // ========== Helper Methods ==========

    /**
     * JSON 문자열을 보기 목록으로 파싱합니다.
     * 
     * @param optionsJson JSON 형식의 보기 목록 문자열
     * @return 보기 목록 (파싱 실패 시 빈 리스트)
     */
    private List<String> parseChoices(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * AI 서버 호출 실패 시 사용할 placeholder 문항을 생성합니다.
     * 
     * @param attemptId 시도 ID
     * @return 샘플 문항 5개
     */
    private List<QuizQuestion> generatePlaceholders(UUID attemptId) {
        List<QuizQuestion> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            QuizQuestion q = new QuizQuestion();
            q.setAttemptId(attemptId);
            q.setQuestionOrder(i); // 순서 설정
            q.setQuestion("샘플 문제 " + (i + 1));
            q.setOptions(toJson(List.of("보기1", "보기2", "보기3", "보기4", "보기5")));
            q.setCorrectOptionIdx(0);
            q.setExplanation("샘플 해설");
            list.add(q);
        }
        return list;
    }

    /**
     * 보기 목록을 JSON 문자열로 변환합니다.
     * 
     * @param choices 보기 목록
     * @return JSON 문자열 (변환 실패 시 "[]")
     */
    private String toJson(List<String> choices) {
        try {
            return objectMapper.writeValueAsString(choices);
        } catch (Exception e) {
            return "[]";
        }
    }
}

