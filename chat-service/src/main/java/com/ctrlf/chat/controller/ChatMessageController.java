package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageCursorResponse;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;
import com.ctrlf.chat.service.ChatMessageService;
import com.ctrlf.common.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * 채팅 메시지 관련 REST API 컨트롤러
 * 
 * <p>채팅 메시지의 전송, 조회, 재시도 등의 기능을 제공합니다.</p>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 메시지 전송 및 AI 응답 생성
     * 
     * <p>사용자가 메시지를 전송하면 AI 서버에 요청을 보내고 응답을 받아 저장합니다.</p>
     * 
     * @param request 메시지 전송 요청 (세션 ID, 메시지 내용)
     * @param jwt JWT 토큰 (사용자 정보 및 도메인 정보 포함)
     * @return 생성된 AI 응답 메시지 정보
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageSendResponse> sendMessage(
        @RequestBody ChatMessageSendRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String domain = jwt.getClaimAsString("domain");
        
        // JWT에서 department 추출 (첫 번째 부서 사용)
        String department = null;
        List<String> departments = SecurityUtils.extractDepartments(jwt);
        if (departments != null && !departments.isEmpty()) {
            department = departments.get(0);
        }

        return ResponseEntity.ok(
            chatMessageService.sendMessage(request, userId, domain, department)
        );
    }

    /**
     * 세션별 메시지 목록 조회 (커서 기반 페이지네이션)
     * 
     * <p>특정 세션의 메시지 목록을 커서 기반 페이지네이션으로 조회합니다.</p>
     * 
     * @param sessionId 세션 ID
     * @param cursor 다음 페이지 커서 (선택적, 첫 페이지 조회 시 null)
     * @param size 페이지 크기 (기본값: 20)
     * @return 메시지 목록 및 페이지네이션 정보
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessageCursorResponse> getMessagesBySession(
        @PathVariable UUID sessionId,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
            chatMessageService.getMessagesBySession(sessionId, cursor, size)
        );
    }

    /**
     * 메시지 재시도
     * 
     * <p>실패한 메시지에 대해 AI 서버에 재요청을 보냅니다.</p>
     * 
     * @param sessionId 세션 ID
     * @param messageId 재시도할 메시지 ID
     * @return 재시도 결과 메시지
     */
    @PostMapping("/sessions/{sessionId}/messages/{messageId}/retry")
    public ResponseEntity<ChatMessage> retryMessage(
        @PathVariable UUID sessionId,
        @PathVariable UUID messageId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        // JWT에서 department 추출 (첫 번째 부서 사용)
        String department = null;
        List<String> departments = SecurityUtils.extractDepartments(jwt);
        if (departments != null && !departments.isEmpty()) {
            department = departments.get(0);
        }
        
        return ResponseEntity.ok(
            chatMessageService.retryMessage(sessionId, messageId, department)
        );
    }

    /**
     * 관리자용 질문 로그 조회 (FAQ 자동 생성용)
     * 
     * <p>AI 서버에서 FAQ 자동 생성을 위해 질문 로그를 조회할 때 사용합니다.</p>
     * <p>여러 사용자 간의 질문 빈도를 분석하기 위해 user role 메시지만 조회합니다.</p>
     * 
     * @param domain 도메인 필터 (선택)
     * @param daysBack 최근 N일간의 데이터 (기본값: 30)
     * @return 질문 로그 목록
     */
    @GetMapping("/admin/messages")
    public ResponseEntity<com.ctrlf.chat.dto.response.AdminMessageLogResponse> getAdminMessages(
        @RequestParam(required = false) String domain,
        @RequestParam(required = false, defaultValue = "30") Integer daysBack
    ) {
        return ResponseEntity.ok(
            chatMessageService.getAdminMessages(domain, daysBack)
        );
    }
}

/**
 * 내부 서비스 간 통신용 채팅 메시지 컨트롤러
 * 
 * <p>AI 서버 등 내부 서비스에서 호출하는 API를 제공합니다.</p>
 * <p>/internal/** 경로는 인증이 필요 없습니다.</p>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/internal/chat")
@RequiredArgsConstructor
class InternalChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 내부 서비스용 질문 로그 조회 (FAQ 자동 생성용)
     * 
     * <p>AI 서버에서 FAQ 자동 생성을 위해 질문 로그를 조회할 때 사용합니다.</p>
     * <p>이 엔드포인트는 인증이 필요 없으며, 내부 서비스 간 통신용입니다.</p>
     * 
     * @param domain 도메인 필터 (선택)
     * @param daysBack 최근 N일간의 데이터 (기본값: 30)
     * @return 질문 로그 목록
     */
    @GetMapping("/admin/messages")
    public ResponseEntity<com.ctrlf.chat.dto.response.AdminMessageLogResponse> getAdminMessages(
        @RequestParam(required = false) String domain,
        @RequestParam(required = false, defaultValue = "30") Integer daysBack
    ) {
        return ResponseEntity.ok(
            chatMessageService.getAdminMessages(domain, daysBack)
        );
    }
}
