package com.ctrlf.education.exception;

import com.ctrlf.common.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 교육 서비스 전역 예외 처리기.
 * 클라이언트에 일관된 오류 응답을 제공합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 잘못된 요청(비즈니스 검증 실패 등).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiError("Bad Request", ex.getMessage()));
    }

    /**
     * 파라미터 타입 변환 실패(예: enum 바인딩 실패 등).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiError("Bad Request", ex.getMessage()));
    }

    /**
     * 파라미터/바디 유효성 검증 실패.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiError("Validation Failed", ex.getMessage()));
    }

    /**
     * 처리되지 않은 서버 오류.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleServerError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError("Internal Server Error", null));
    }
}


