package com.ctrlf.education.quiz.controller;

import static com.ctrlf.education.quiz.dto.QuizRequest.*;
import static com.ctrlf.education.quiz.dto.QuizResponse.*;

import com.ctrlf.education.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // NOTE: 사용자 식별은 간단히 헤더로 전달받습니다. (예: X-User-UUID)
    private UUID parseUser(String header) {
        return UUID.fromString(header);
    }

    @GetMapping("/{eduId}/start")
    @Operation(summary = "퀴즈 시작(문항 생성/복원)")
    public ResponseEntity<StartResponse> start(
        @PathVariable("eduId") UUID educationId,
        @RequestHeader("X-User-UUID") String userHeader
    ) {
        return ResponseEntity.ok(quizService.start(educationId, parseUser(userHeader)));
    }

    @PostMapping("/attempt/{attemptId}/submit")
    @Operation(summary = "퀴즈 제출/채점")
    public ResponseEntity<SubmitResponse> submit(
        @PathVariable("attemptId") UUID attemptId,
        @RequestHeader("X-User-UUID") String userHeader,
        @RequestBody SubmitRequest req
    ) {
        return ResponseEntity.ok(quizService.submit(attemptId, parseUser(userHeader), req));
    }

    @GetMapping("/attempt/{attemptId}/result")
    @Operation(summary = "퀴즈 결과 조회")
    public ResponseEntity<ResultResponse> result(
        @PathVariable("attemptId") UUID attemptId,
        @RequestHeader("X-User-UUID") String userHeader
    ) {
        return ResponseEntity.ok(quizService.result(attemptId, parseUser(userHeader)));
    }

    @GetMapping("/{attemptId}/wrongs")
    @Operation(summary = "오답노트 목록 조회")
    public ResponseEntity<List<WrongNoteItem>> wrongs(
        @PathVariable("attemptId") UUID attemptId,
        @RequestHeader("X-User-UUID") String userHeader
    ) {
        return ResponseEntity.ok(quizService.wrongs(attemptId, parseUser(userHeader)));
    }

    @PostMapping("/attempt/{attemptId}/leave")
    @Operation(summary = "퀴즈 이탈 기록")
    public ResponseEntity<LeaveResponse> leave(
        @PathVariable("attemptId") UUID attemptId,
        @RequestHeader("X-User-UUID") String userHeader,
        @RequestBody LeaveRequest req
    ) {
        return ResponseEntity.ok(quizService.leave(attemptId, parseUser(userHeader), req));
    }
}

