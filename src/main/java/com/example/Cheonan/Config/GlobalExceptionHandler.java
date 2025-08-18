// src/main/java/com/example/Cheonan/Exception/GlobalExceptionHandler.java
package com.example.Cheonan.Exception;

import com.example.Cheonan.Dto.ApiError;
import com.example.Cheonan.Dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse(ErrorCode.VALIDATION_ERROR.getDefaultMessage());
        log.warn("[400] {}", msg);
        return build(ErrorCode.VALIDATION_ERROR, msg, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .orElse(ErrorCode.VALIDATION_ERROR.getDefaultMessage());
        log.warn("[400] {}", msg);
        return build(ErrorCode.VALIDATION_ERROR, msg, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = String.format("필수 파라미터 '%s'가 없습니다.", e.getParameterName());
        log.warn("[400] {}", msg);
        return build(ErrorCode.MISSING_PARAMETER, msg, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = String.format("파라미터 '%s' 타입이 올바르지 않습니다.", e.getName());
        log.warn("[400] {}", msg);
        return build(ErrorCode.TYPE_MISMATCH, msg, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaType(HttpMediaTypeNotSupportedException e) {
        log.warn("[415] {}", e.getMessage());
        return build(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type 입니다.", e.getMessage());
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalHttp(HttpStatusCodeException e) {
        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            log.warn("[429] External API quota: {}", e.getResponseBodyAsString());
            return build(ErrorCode.EXTERNAL_API_QUOTA, "외부 API 사용량을 초과했어요.", e.getResponseBodyAsString());
        }
        log.error("[Bad Gateway] External API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        return build(ErrorCode.EXTERNAL_API_ERROR, "외부 API 호출에 실패했어요.", e.getResponseBodyAsString());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        String detail = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : null;
        log.warn("[409] Data integrity: {}", detail);
        return build(ErrorCode.DATA_INTEGRITY, "데이터 제약 조건에 맞지 않아요.", detail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception e) {
        log.error("[500] Unhandled exception", e);
        return build(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), null);
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode code, String message, String detail) {
        ApiError err = ApiError.builder()
                .code(code.name()) // 문자열 코드 (예: VALIDATION_ERROR)
                .message(message != null ? message : code.getDefaultMessage())
                .detail(detail)
                .build();
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(err));
    }

    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<Void> handleNoContent(NoContentException e) {
        return ResponseEntity.noContent().build(); // 204 반환
    }
}